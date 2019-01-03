# ngoy web site

[ngoy](https://github.com/krizzdewizz/ngoy) is a template engine for the JVM, based on the Angular component architecture.

This is the source of ngoy's web site. Made with ngoy...of course ;)

You can visit the site [here](https://krizzdewizz.github.io/ngoy-website/).

## Development

I used to:

- Import the `gradle` project into Eclipe
- Right-click on the project and choose `Debug As/Spring Boot App`
- Open [http://localhost:8080](http://localhost:8080)
- Make changes in code/HTML and hit F5 in the browser to refresh. Eclipse Hot Code Replace will pick up the changes

## Build

Render the site into the `docs` folder:

```
gradle renderSite
```
