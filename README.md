# AI-Dating-App-Filter
A program designed to make browsing dating apps easier and less time-consuming by using a local LLM to filter matches.

Requires Ollama and Google Chrome to be installed.

You are more than welcome to make pull requests to add support for more apps or operating systems because I'm too lazy to do all of them. There is an abstract page-object-model superclass named ProfilePage.java that follows the format of most dating apps, meaning all you need to do to add support for an one is to extend it and specify how to get the data for the abstract accessors via Selenium, then register it in Constants.java.

Disclaimer: The sole purpose of this software is to automate an otherwise time-consuming process by filtering the users. The program itself does not interact on social platforms like bots do, because this program surrenders control to the user when it identifies a likely match. Therefore, this system does not pollute dating apps with bot interactions but only expedites something the human user likely would have done anyways.
