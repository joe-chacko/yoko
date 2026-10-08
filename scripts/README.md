# scripts/

Utility shell scripts for CI, branch management, and tooling.

---

## 📋 Script style guide

All scripts in this directory follow a common style.  New scripts **must** conform to it.

---

### 🔧 Shebang

Always use `/bin/sh` — never `bash`, `zsh`, or anything else.  This keeps scripts
portable across every environment the CI pipeline runs in.

```sh
#!/bin/sh
```

Leave a **blank line** between the shebang and the copyright block.

---

### 📄 License header

Every script carries the full Apache 2.0 licence block followed by the SPDX
identifier:

```sh
#!/bin/sh

# Copyright 2026 IBM Corporation and others.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
```

---

### 🔒 Top-level subshell

Wrap **all** script logic inside a `( … )` subshell.  This prevents any
`set` options, variable assignments, or `cd` calls from leaking into the caller's
shell if the script is ever sourced instead of executed.

```sh
# Enforce top-level subshell to avoid leaking environment changes (in case script is sourced)
(
  # Stop on first unexpected error
  set -e
  # Disable globbing
  set -f

  …script body…
)
```

The two `set` lines are **always** the first thing inside the subshell:

| option | purpose |
|--------|---------|
| `set -e` | exit immediately on any unexpected non-zero return code |
| `set -f` | disable filename globbing — prevents accidental expansion of unquoted `*` or `?` |

---

### 💀 `die()` helper

Define `die` as a multi-line block function.  Pass the message via `$@` (preserves
word boundaries across arguments) and write to `stderr`:

```sh
  die() {
    echo "$@" >&2
    exit 1
  }
```

**Do not** use the one-liner form `die() { echo "Error: $*" >&2; exit 1; }` — it
diverges from the block style used in the rest of the codebase and the hardcoded
`"Error: "` prefix prevents callers from composing their own message prefix.

Usage:

```sh
  command -v git > /dev/null 2>&1 || die "Can not find 'git' command."
```

---

### 🪵 Logging (optional, for complex scripts)

Simple scripts use plain `echo`.  Scripts that need tiered verbosity (like
[`check-copyright.sh`](check-copyright.sh)) set up named file descriptors and
semantic logging helpers:

```sh
  exec 3>/dev/null 4>&1 5>&1 6>&2
  log() { { [ $# -gt 0 ] && echo "$@"; } || cat; >&3; }
  inf() { { [ $# -gt 0 ] && echo "$@"; } || cat; >&4; }
  wrn() { { [ $# -gt 0 ] && echo "$@"; } || cat; >&5; }
  err() { { [ $# -gt 0 ] && echo "$@"; } || cat; >&6; }
```

Only add this machinery when the script genuinely needs multiple verbosity levels.

---

### 😀 Emoji in output

Use emoji in `echo` output to make it easy to scan at a glance.  Conventions used
across this codebase:

| emoji | meaning |
|-------|---------|
| ✓     | success / already done |
| ⚠     | warning — something to be aware of but not fatal |
| 🫥    | informational skip (deleted / excluded file) |
| 👿 😡 🤬 | graded error severity in copyright output |
| 😅    | copyright OK (informational) |
| 🤯    | unexpected/unsupported condition |
| ‼️    | script may need manual attention |

---

### 📐 Indentation

Use **two spaces** inside the top-level subshell.  Nested blocks indent by a
further two spaces per level.  Do not use tabs.

---

### 📝 Complete template

```sh
#!/bin/sh

# Copyright YYYY IBM Corporation and others.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0

# Enforce top-level subshell to avoid leaking environment changes (in case script is sourced)
(
  # Stop on first unexpected error
  set -e
  # Disable globbing
  set -f

  die() {
    echo "$@" >&2
    exit 1
  }

  # … your script here …
)
```
