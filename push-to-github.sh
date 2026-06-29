#!/bin/bash

# GitHub Push Script for Mimo GST Billing
# Usage: ./push-to-github.sh

echo "======================================"
echo "  Push Mimo GST Billing to GitHub"
echo "======================================"
echo ""

# Check if gh is logged in
if ! gh auth status > /dev/null 2>&1; then
    echo "Please login to GitHub first:"
    echo "  gh auth login"
    echo ""
    echo "Follow the prompts to authenticate."
    exit 1
fi

# Get current user
USERNAME=$(gh api user -q .login)
REPO_NAME="mimo-gst-billing"

echo "Creating repository: $USERNAME/$REPO_NAME"
echo ""

# Create repo (public)
gh repo create "$REPO_NAME" \
    --public \
    --description "Indian GST Billing Android App - Built with Jetpack Compose & Hilt" \
    --source=. \
    --push

if [ $? -eq 0 ]; then
    echo ""
    echo "Success! Repository pushed to:"
    echo "  https://github.com/$USERNAME/$REPO_NAME"
    echo ""
    echo "You can now:"
    echo "  - Visit the repo online"
    echo "  - Clone it on any machine"
    echo "  - Enable GitHub Actions for CI/CD"
else
    echo "Error! Could not push to GitHub."
    echo "Trying manual git push..."
    
    git remote add origin "https://github.com/$USERNAME/$REPO_NAME.git" 2>/dev/null
    git push -u origin main
fi
