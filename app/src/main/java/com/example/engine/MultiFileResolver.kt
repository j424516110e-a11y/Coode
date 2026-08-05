package com.example.engine

import com.example.data.CodeFileEntity

class MultiFileResolver {

    /**
     * Resolves all HTML, CSS, and JS files into a single unified HTML document
     * for live preview in the WebView. Handles <script src="..."> and <link rel="stylesheet">.
     */
    fun resolveWebBundle(files: List<CodeFileEntity>, activeFile: CodeFileEntity?): String {
        val htmlFile = files.firstOrNull { it.name.endsWith(".html") }
            ?: activeFile?.takeIf { it.name.endsWith(".html") }

        var htmlContent = htmlFile?.content ?: """
            <!DOCTYPE html>
            <html lang="ar" dir="rtl">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>CodeCanvas App</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background-color: #0d1117;
                        color: #c9d1d9;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        height: 100vh;
                        margin: 0;
                    }
                    .card {
                        background: #161b22;
                        padding: 24px;
                        border-radius: 12px;
                        border: 1px solid #30363d;
                        text-align: center;
                    }
                </style>
            </head>
            <body>
                <div class="card">
                    <h2>🚀 معاينة التطبيق</h2>
                    <p>قم بكتابة كود HTML أو JS لعرض المعاينة المباشرة هنا.</p>
                </div>
            </body>
            </html>
        """.trimIndent()

        // Replace CSS link tags with inline styles from files in the project
        val cssFiles = files.filter { it.name.endsWith(".css") }
        for (cssFile in cssFiles) {
            val fileName = cssFile.name
            val linkRegex = Regex("""<link[^>]+href=["'](.*?""" + Regex.escape(fileName) + """*)["'][^>]*>""")
            val styleBlock = "<style>\n/* Inline: ${cssFile.name} */\n${cssFile.content}\n</style>"
            if (linkRegex.containsMatchIn(htmlContent)) {
                htmlContent = linkRegex.replace(htmlContent, styleBlock)
            } else {
                htmlContent = htmlContent.replace("</head>", "$styleBlock\n</head>")
            }
        }

        // Replace JS script tags with inline scripts from files in the project
        val jsFiles = files.filter { it.name.endsWith(".js") }
        for (jsFile in jsFiles) {
            val fileName = jsFile.name
            val scriptRegex = Regex("""<script[^>]+src=["'](.*?""" + Regex.escape(fileName) + """*)["'][^>]*>[\s\S]*?</script>""")
            val scriptBlock = "<script>\n// Inline: ${jsFile.name}\n${jsFile.content}\n</script>"
            if (scriptRegex.containsMatchIn(htmlContent)) {
                htmlContent = scriptRegex.replace(htmlContent, scriptBlock)
            } else {
                htmlContent = htmlContent.replace("</body>", "$scriptBlock\n</body>")
            }
        }

        // Inject Console Bridge for live logger interception
        val consoleBridge = """
            <script>
                (function() {
                    var oldLog = console.log;
                    var oldError = console.error;
                    var oldWarn = console.warn;
                    
                    function sendToApp(type, message) {
                        if (window.AndroidConsole) {
                            window.AndroidConsole.postMessage(JSON.stringify({type: type, msg: message}));
                        }
                    }
                    
                    console.log = function() {
                        var msg = Array.prototype.slice.call(arguments).join(' ');
                        sendToApp('log', msg);
                        oldLog.apply(console, arguments);
                    };
                    console.error = function() {
                        var msg = Array.prototype.slice.call(arguments).join(' ');
                        sendToApp('error', msg);
                        oldError.apply(console, arguments);
                    };
                    console.warn = function() {
                        var msg = Array.prototype.slice.call(arguments).join(' ');
                        sendToApp('warn', msg);
                        oldWarn.apply(console, arguments);
                    };
                })();
            </script>
        """.trimIndent()

        htmlContent = htmlContent.replace("<head>", "<head>\n$consoleBridge")
        return htmlContent
    }

    /**
     * Resolves Python multi-file modules by concatenating module definitions or replacing imports.
     */
    fun resolvePythonBundle(files: List<CodeFileEntity>, mainFileContent: String): String {
        val pyFiles = files.filter { it.name.endsWith(".py") }
        val moduleHeader = StringBuilder()

        for (pyFile in pyFiles) {
            val moduleName = pyFile.name.removeSuffix(".py")
            moduleHeader.append("# --- Module: $moduleName ---\n")
            moduleHeader.append(pyFile.content)
            moduleHeader.append("\n\n")
        }

        return moduleHeader.toString()
    }
}
