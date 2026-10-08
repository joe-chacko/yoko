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

  # Unlocks the dev branch.
  # Direct pushes blocked — all changes must go through a PR with 1 approving review.
  # Reads the existing protection state and only flips lock_branch and enforce_admins,
  # preserving all other settings (status checks, PR reviews, restrictions, etc.).

  command -v jq > /dev/null 2>&1 || die "jq is required but not found."

  # Fetch current protection and transform it into a valid PUT body:
  #   - set lock_branch = false
  #   - set enforce_admins = false
  #   - unwrap nested .required_status_checks and .required_pull_request_reviews objects
  #     into the flat form the PUT endpoint expects
  BODY="$(gh api repos/OpenLiberty/yoko/branches/dev/protection | jq '{
    required_status_checks: (
      if .required_status_checks then {
        strict: .required_status_checks.strict,
        contexts: .required_status_checks.contexts
      } else null end
    ),
    enforce_admins: false,
    required_pull_request_reviews: (
      if .required_pull_request_reviews then {
        dismissal_restrictions:           .required_pull_request_reviews.dismissal_restrictions,
        dismiss_stale_reviews:            .required_pull_request_reviews.dismiss_stale_reviews,
        require_code_owner_reviews:       .required_pull_request_reviews.require_code_owner_reviews,
        required_approving_review_count:  .required_pull_request_reviews.required_approving_review_count,
        require_last_push_approval:       .required_pull_request_reviews.require_last_push_approval
      } else null end
    ),
    restrictions: (
      if .restrictions then {
        users: (.restrictions.users | map(.login)),
        teams: (.restrictions.teams | map(.slug)),
        apps:  (.restrictions.apps  | map(.slug))
      } else null end
    ),
    lock_branch: false
  }')"

  echo "$BODY" | gh api repos/OpenLiberty/yoko/branches/dev/protection \
    --method PUT \
    --input -

  echo "🔓 dev branch unlocked. PRs required with 1 approving review."
)
