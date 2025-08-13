## [v1.5.0]

### 🚀 Features
- Logging improvements using new `yoko.verbose.*` loggers
- Detect IBM Java ORB and marshal `java.util.Date` sympathetically

### 🐛 Bug Fixes
- Set SO_REUSEADDR from DefaultConnectionHelper
- Improve little endian handling in ReadBuffer
- Send service contexts on first GIOP 1.2 message
- Support java transaction exceptions if present
- Use local codebase for collocated invocations
- Make CodeBaseProxy.getCodeBase() public
- Stop infinite recursion in TypeCode.toString()
- Remove unnecessary throws declarations
- Marshal non-serializable fields as abstract values
- Don't nest INTERNAL exceptions needlessly
- Avoid processing fields for non-serializable classes
- Always setReuseAddress(true) on server sockets
- Unmarshal String in Comparable field
