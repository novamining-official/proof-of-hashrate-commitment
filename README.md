# Proof-of-Hashrate-Commitment

## Overview

This tool aims to help cloud mining providers to prove their fairness toward the users of the pool and is designed as a microservice, docker images can be built via the provided Dockerfile. The idea is based on the liability proof suggested by Gregory Maxwell. Simply put, given a set of users of a mining pool and their individual hashrate, we encode this information info a merkle-tree data structure of which the pool publishes the root digest in the coinbase of a mined block, then every user is provided with its branch of the tree as a Proof-of-Hashrate-Commitment.
Implementation details and tech specs can be found in _attachment_1_. You can create trees by PUTting a list of "Accounts" via the API, accounts are comprised of {user, balance, nonce} with balance being a floating point number. Is also possible to retrieve a Proof for a user given its account and the stored tree's root-digest, likewise you can ask the API to verify a Proof for a given account.

## Development

The application is written in Scala and requires JDK8 and sbt. The data is stored both in memory and on disk, the disk storage is in plain text (JSON format). 


## Installation

The packaging is done via <https://www.scala-sbt.org/sbt-native-packager>, you can produce a self contained executable (packed in a tarball) by issuing: `sbt universal:packageZipTarball`, this will create a `.tar.gz` inside `"target/universal"` folder, to run the application just use the launchers in `'bin'` folder. Custom parameters can be provided via command-line as in `-D api.port=8088`, a complete list can be found in application.conf file.

## License

[MIT License (MIT)](https://opensource.org/licenses/MIT) - Copyright 2018 NovaMining Ltd.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

## Contributions

Contributions are welcome under form of developers, documentation writers and donations. When contributing to this repository, please first discuss the change you wish to make via issue, email, or any other method with the owners of this repository before making a change.

Please note we have a code of conduct, please follow it in all your interactions with the project.

### Pull Request Process

1. Ensure any install or build dependencies are removed before the end of the layer when doing a build.
2. Update the README.md with details of changes to the interface, this includes new environment variables, exposed ports, useful file locations and container parameters.
3. Increase the version numbers in any examples files and the README.md to the new version that this Pull Request would represent.
4. You may merge the Pull Request in once you have the sign-off of two other developers, or if you do not have permission to do that, you may request the second reviewer to merge it for you.

### Code of Conduct

### Our Pledge

In the interest of fostering an open and welcoming environment, we as contributors and maintainers pledge to making participation in our project and our community a harassment-free experience for everyone, regardless of age, body size, disability, ethnicity, gender identity and expression, level of experience, nationality, personal appearance, race, religion, or sexual identity and orientation.

### Our Standards

Examples of behavior that contributes to creating a positive environment include:

 - Using welcoming and inclusive language
 - Being respectful of differing viewpoints and experiences
 - Gracefully accepting constructive criticism
 - Focusing on what is best for the community
 - Showing empathy towards other community members

Examples of unacceptable behavior by participants include:

 - The use of sexualized language or imagery and unwelcome sexual attention or advances
 - Trolling, insulting/derogatory comments, and personal or political attacks
 - Public or private harassment
 - Publishing others' private information, such as a physical or electronic address, without explicit permission
 - Other conduct which could reasonably be considered inappropriate in a professional setting

### Our Responsabilities

Project maintainers are responsible for clarifying the standards of acceptable behavior and are expected to take appropriate and fair corrective action in response to any instances of unacceptable behavior.

Project maintainers have the right and responsibility to remove, edit, or reject comments, commits, code, wiki edits, issues, and other contributions that are not aligned to this Code of Conduct, or to ban temporarily or permanently any contributor for other behaviors that they deem inappropriate, threatening, offensive, or harmful.

### Scope

This Code of Conduct applies both within project spaces and in public spaces when an individual is representing the project or its community. Examples of representing a project or community include using an official project e-mail address, posting via an official social media account, or acting as an appointed representative at an online or offline event. Representation of a project may be further defined and clarified by project maintainers.

### Enforcement

Instances of abusive, harassing, or otherwise unacceptable behavior may be reported by contacting the project team at [devops@novamining.io](mailto:devops@novamining.io). All complaints will be reviewed and investigated and will result in a response that is deemed necessary and appropriate to the circumstances. The project team is obligated to maintain confidentiality with regard to the reporter of an incident. Further details of specific enforcement policies may be posted separately.

Project maintainers who do not follow or enforce the Code of Conduct in good faith may face temporary or permanent repercussions as determined by other members of the project's leadership.

### Attribution

This Code of Conduct is adapted from the [Contributor Covenant](https://contributor-covenant.org/), version 1.4, available at http://contributor-covenant.org/version/1/4.

### Donations

Bitcoin: `35wVmfBS7QB6dx8KkKuFfBSNPbg5hTuaKc`


