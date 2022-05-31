Narrative:
As a user
I want to test my story

Scenario: Test Jbehave scenario 1
Meta:
@author pradeep
@priority high
Given user opens home page
When user enters into search box with next '<keyword>'
And user clicks on search button
Then all the result titles should contain the word <keyword>

Examples:
| keyword |
| JBehave |
