# Polly_App
  Polly is an app which plays words based on the difficulty level (easy/medium/hard) chosen.
  The kid will have to spell the word by writing on the space provided.
  The Polly will then tell if the word spelled is right or wrong.

# How does it work?
  A level has to be chosen according to which the words set is formed.
  The words are then played one by one by invoking "Polly" everytime Play/Prev/Next button is clicked.
  The kid can then type the word on the space provided and submit it, to which Polly will respond as either right or wrong
  
# Number of Activities?
 There are a total of  activities:
  1. Splash Activity 
      a. mainly used to demonstrate the concept of Splash and creating a thread!
  2. Choose Level
      a. The kid will have to choose any one of the level (easy/medium/hard)
      b. The level is then passed to the server where the words are retrived from the database
  3. MainActivity
      a. OnCreation of this activity
          1. The Polly is initialized
          2. The media Player is set
      b. Based on the button clicked, the listeners will act accordingly and make a call to Polly using the synthesizeSpeech() method which takes in 3 parameters: 
          1. Voice Id (Aditi-Indian English voice)
          2. Word (word sent as an argument)
          3. Format (mp3 format)
      c. The words are retrieved from the server as a CSV (Comma Separated Values)

# Amazon Web Services used
  1. Amazon Polly
  2. Amazon Incognito pool
  3. Amazon EC2 (Elastic Cloud Compute)
  4. Amazon RDS (Relational Database Service)
  
# Problems encountered
  1. networkOnMainThreadException()
      -This error occurs mainly when the UI thread or the main thread is doing a lot of heavy processing.
  2. Huge number of frames being skipped.

# Solution found
  1. Heavy processing includes making network calls like Http Requests, loading URL's etc.
      All of these works can be done in background using the java's AsyncTask class which consists of a doInBackground() method.
      The class making the network calls will ve to extend the AsyncTask class and Override its doInBackground() method.
      This makes sure, all of the network calls are dome in background and not on the UI thread.
      
  2. Frames being skipped is a tricky error one can get.
      When searched online for solutions, they said the heavy processing of tasks should be done in background with the help of AsyncTask.
      Despite of doing that, defragmenting tasks into different AsyncTask classes, the error was still visible.
      It made the app very slow(more than the usual speed categorised as slow)
      
      The ultimate solution was the IMAGE SIZE used for the app as the background image, various buttons etc.
      The image sizes, if is more than few MBs(mine was 20 to 50 MB), reduces the speed.
      Becasue the UI thread will take time to load such a big file every time you move from one frame to another or you type a word from the keypad and close the keypad.
      I then resized the images to the standard images sizes to use in an app (I browsed online to check for the sizes)
      

