# PodcastProject

This application should be used for an easy to setup all in one podcast website including additions like
a wiki, advent calendars, different podcasts (feeds, specials, etc.)

## Running the application

The project is a standard Maven project. To run it from the command line,
type `mvnw` (Windows), or `./mvnw` (Mac & Linux), then open
http://localhost:8080 in your browser.

You can also import the project to your IDE of choice as you would with any
Maven project. Read more
on [how to import Vaadin projects to different IDEs](https://vaadin.com/docs/latest/guide/step-by-step/importing) (
Eclipse, IntelliJ IDEA, NetBeans, and VS Code).

## Settings

### Environment Variables

This application uses environment variables for basic settings.

| Environment Variable   | Default | Required | Description                                                                  |
|------------------------|---------|----------|------------------------------------------------------------------------------|
| PORT                   | 8080    | no       | Sets the port the application runs on                                        |
| PODCAST_PROJECT_CONFIG |         | yes      | Sets the path to the folder containing the [settings files](#settings-files) |
| DB_URL                 |         | yes      | Sets the jdbc url for the database connection used for user management, etc  |
| DB_USERNAME            | ""      | no       | Sets the username used for database connection                               |
| DB_PASSWORD            | ""      | no       | Sets the password used for database connection                               |

## Settings files

The application loads all `.json`-files in the provided settings folder as settings, where the name is used as the key,
if you want to access a custom settings-file while modifying or extending the application.

### Expected configurations

`application.json`

``` json
{
  "name": "Name of your porcast project"
}
```

`podcasts.json`

``` json
{
  "perma-key-of-your-podcast": {
    "url": "https://example.podcast.com/rss (Required)",
    "clients": {
      "key-for-service-1": {
        "key": "value (See below)"
      },
      "key-for-service-2": {
      }
    }
  }
}
```

#### Supported services to follow podcast

following are the available services you can set so your podcast can be followed with their respective data you
have to provide for the element to work

| Service | Key | Default | Required | Description                                                     |
|---------|-----|---------|----------|-----------------------------------------------------------------|
| spotify | id  |         | yes      | ID of your spotify podcast (https://open.spotify.com/show/<ID>) |

# TODOs

- Responsive layout for better viewing on mobile devices
- Custom web player based on HTML5
- Services to subscribe
- Management interface for admins
- Additional roles like proof listeners, editors, podcasters etc.
- Wiki for podcast and podcast topics.
  - Wiki can change content based on podcast episode
- Support for filtering in podcast view
  - specials, etc

# Attributions

## Images

<a href="https://www.vecteezy.com/free-png/default-avatar">Default Avatar PNGs by Vecteezy</a>

<a href="https://www.flaticon.com/free-icons/audio" title="audio icons">Audio icons created by Freepik - Flaticon</a>
