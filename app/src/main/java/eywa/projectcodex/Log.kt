package eywa.projectcodex

class Log {
    companion object {
        private const val TAG = "PROJECT_CODEX"

        fun d(tag: String, msg: String): Int {
            val print = "DEBUG: ${TAG}_$tag: $msg"
            println(print)
            return print.length
        }

        fun i(tag: String, msg: String): Int {
            val print = "INFO: ${TAG}_$tag: $msg"
            println(print)
            return print.length
        }

        fun w(tag: String, msg: String): Int {
            val print = "WARN: ${TAG}_$tag: $msg"
            println(print)
            return print.length
        }

        fun e(tag: String, msg: String): Int {
            val print = "ERROR: ${TAG}_$tag: $msg"
            println(print)
            return print.length
        }
    }
}