package com.example.movielab.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

class MovieLabIssueRegistry : IssueRegistry() {
    override val issues: List<Issue> = MovieLabIssues.all

    override val api: Int = CURRENT_API

    override val minApi: Int = 14

    override val vendor: Vendor = Vendor(
        vendorName = "android-skill-lint-lab",
        identifier = "com.example.movielab:checks",
        feedbackUrl = "https://github.com/hellokai55/android-skill-lint-lab",
    )
}
