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
# distributed under the License is distributed on an \"AS IS\" BASIS,
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

  # Locks the dev branch read-only (Option 3).
  # No pushes allowed — not even via PR, not even by admins.

  gh api repos/OpenLiberty/yoko/branches/dev/protection \
    --method PUT \
    --input - <<'EOF'
{
  "required_status_checks": null,
  "enforce_admins": true,
  "required_pull_request_reviews": null,
  "restrictions": null,
  "lock_branch": true
}
EOF

  echo "dev branch is now fully locked (read-only)."
)
