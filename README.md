
<h1 align="center">
  <br>
  <a href="https://github.com/quangson91/intellij_rest_cli"><img src="images/logo.png" alt="restcli" width="200"></a>
  <br>
  restcli
  <br>
</h1>

<h4 align="center">A missing commandline application for execute <a href="https://www.jetbrains.com/help/idea/http-client-in-product-code-editor.html" target="_blank">Intellij HTTP Client file</a>.</h4>

<p align="center">
  <a href="https://github.com/quangson91/intellij_rest_cli/releases/tag/1.0">
    <img src="https://img.shields.io/badge/restcli-1.0-brightgreen"
         alt="restcli">
  </a>
  <a href="https://github.com/quangson91/intellij_rest_cli/blob/master/LICENSE">
      <img src="https://img.shields.io/badge/license-mit-blue"
           alt="restcli">
    </a>    
  <a href="https://paypal.me/quangson8128">
    <img src="https://img.shields.io/badge/$-donate-ff69b4.svg?maxAge=2592000&amp;style=flat">
  </a>
</p>

<p align="center">
  <a href="#key-features">Key Features</a> •
  <a href="#how-to-use">How To Use</a> •
  <a href="#download">Download</a> •
  <a href="#credits">Credits</a> •
  <a href="#license">License</a> •
  <a href="https://www.producthunt.com/posts/intellij-rest-cli">Producthunt</a>
</p>

![screenshot](images/restcli_screenshots.png)

## Key Features
* Execute Intellij HTTP request files
* Running test script including:
    - Embedded script inside HTTP request file
    - Include external javascript test file
* Loading and inject environment variables from
    - http-client.env.json
    - http-client.private.env.json
* Cross platform
  - Windows, macOS and Linux ready.
* Easy to custom via commandline arguments
  - Custom logging request
  - Inject environment name

## How To Use

The fastest way to get rest cli is download jar from [releases tab](https://github.com/quangson91/intellij_rest_cli/releases)

```
Usage: restcli [-hV] [-e=<environmentName>] [-l=<logLevel>] -s=<httpFilePath>
Intellij Restcli
  -e, --env=<environmentName>
                  Name of the environment in config file
                  (http-client.env.json/http-client.private.env.json).
  -h, --help      Show this help message and exit.
  -l, --log-level=<logLevel>
                  Config log level while the executor running.
                  Valid values: NONE, BASIC, HEADERS, BODY
  -s, --script=<httpFilePath>
                  Path to the http script file.
  -V, --version   Print version information and exit.
```

#### Example
```bash
# Move to folder that contains your http files.
$ cd requests

$ tree
├── get-requests.http
├── http-client.env.json
├── http-client.private.env.json
├── post-requests.http
├── request-form-data.json
├── requests-with-authorization.http
├── requests-with-tests.http
└── test_script.js

$ java -jar /path/to/restcli-1.0.jar -e "test" -s get-requests.http
```

Note: This application required you install `java` on your machine.

## Download

You can [download](https://github.com/quangson91/intellij_rest_cli/releases) the latest version of restcli for Windows, macOS and Linux.

## Credits

This software uses the following open source packages:

- [Jflex](https://jflex.de/) -  a lexical analyzer generator (also known as scanner generator) for Java.
- [PicoCli](https://picocli.info/) - a mighty tiny command line interface
- [okhttp](https://github.com/square/okhttp) - the way modern applications network. It’s how we exchange data & media. Doing HTTP efficiently makes your stuff load faster and saves bandwidth.

## Support

If you need help, please don't hesitate to [file an issue](https://github.com/quangson91/intellij_rest_cli/issues/new).
 

## Sponsoring

This application is free and can be used for free, open source and commercial applications. `restcli` is under the MIT License (MIT). So hit the magic ⭐ button, I appreciate it!!! 🙏

If this project help you, you can give me a cup of coffee :)
<a href="https://paypal.me/quangson8128">
    <img src="https://img.shields.io/badge/$-donate-ff69b4.svg?maxAge=2592000&amp;style=flat">
</a>

<a href="https://www.buymeacoffee.com/quangson91" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/default-orange.png" alt="Buy Me A Coffee" height="41" width="174"></a>
<a href="https://www.patreon.com/quangson91">
	<img src="https://c5.patreon.com/external/logo/become_a_patron_button@2x.png" width="160">
</a>
<a href="https://www.producthunt.com/posts/intellij-rest-cli?utm_source=badge-featured&utm_medium=badge&utm_souce=badge-intellij-rest-cli" target="_blank"><img src="https://api.producthunt.com/widgets/embed-image/v1/featured.svg?post_id=226394&theme=light" alt="IntelliJ Rest CLI - A missing command line application for execute for IntelliJ | Product Hunt Embed" style="width: 250px; height: 54px;" width="250px" height="54px" /></a>

## Contributing
I appreciate your support and feedbacks!

Please file issues if you find bugs and have feature requests. If you are able to send small PRs to improve or fix bugs, that would be awesome too.

For larger PRs, please ping [@quangson91](https://twitter.com/quangson91) to discuss first.

## TODO
There are still many things will be bring in the next versions such as:

- [ ] Build native application by using graalvm.
- [ ] Remove usage of nashorn engine.
- [ ] Support call other requests by name.
- [ ] Or [File an issue](https://github.com/quangson91/intellij_rest_cli/issues/new).

## License

MIT License
```
Copyright (c) 2020 Duong Quang Son

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
---

<h2 class="text-white mb-4">Made with <span class="heart">❤</span> by quangson91</h2>

> [quangson91.gitbook.io](https://quangson91.gitbook.io/uos/) &nbsp;&middot;&nbsp;
> GitHub [@quangson91](https://github.com/quangson91) &nbsp;&middot;&nbsp;
> Twitter [@quangson91](https://twitter.com/quangson91)
