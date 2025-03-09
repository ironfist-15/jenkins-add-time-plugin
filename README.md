# jenkins-add-time-to commit-plugin

This plugin created using jenkins takes the last commit in the repository in which the build file is present and appends the time at which build was made.

Now for the timestamp to be appended, every time the project in this file must be build .
I am in the process of automating this step so that every time a commit is made the build gets triggered automatically and there is no need to manually trigger a build.
The archtype used is empty plugin.

Note:-To avoid confusion first commit then trigger the build.
