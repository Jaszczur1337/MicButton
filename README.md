# MicButton
Extended image view for speech to text

# Usage
Download MicButton class and paste it to your project
```
micButton.myLanguage = "en-US" 
micButton.micListener = object:MicListener{
  override fun onResultFound(result:ArrayList<String>)
  {
  //Do your stuff
  }
}
```
