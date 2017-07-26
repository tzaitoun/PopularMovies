# Project: PopularMovies 


***************************************************************************************************************************
Note: You will need an API key from https://www.themoviedb.org, please insert your key in MovieNetworkUtils.java at line 14 
in the API_KEY variable.  
***************************************************************************************************************************

## Project Description

**Summary:** An application that displays movies in a grid and movie details when a movie is selected in the grid.

**More Details**
- Movie data/information is retrieved from the TMDB in JSON format and it is parsed to be displayed to the user. 
- In the movie details screen, a user has the ability to watch the trailer of the movie through the internet or youtube. The user can also 
  bookmark a movie, so that its information can be viewed offline. 
- The user can view the most popular movies (Top 20 only), the top rated movies (Top 20 only), and bookmarked movies. This is done through   the settings option using SharedPreferences.    
- Bookmarking a movie saves the movie information on an SQLite database and the movie poster image on the device's storage. Unbookmarking a   movie removes all its data from device. If the user is offline and in the bookmarks section, they can view movie information/poster with   the exception of the user reviews, trailer, and backdrop image. However, if the user is online, all other data is retrieved from the       network.  

### Libraries/APIs used 

- themoviedb.org 
- Picasso 

**Special Thanks to Udacity and Udacity Reviewers for providing useful feedback and suggestions on improvements and further learning.**
