# nfc-card-reader

Group project for IMT 3673


## Notice
This is for educational purposes ***only***. Do not abuse. :)

If you ***do*** decide to abuse this, the original creators can not be held 
responsible.


## Group

Group number [#3 from Project Groups](http://prod3.imt.hig.no/teaching/imt3673-2019/wikis/Project-Groups#3-nfc-card-emulator) on the wiki.

* Leon Cinquemani
* Håkon Schia


## The idea

Idea [#6 from Project Ideas](http://prod3.imt.hig.no/teaching/imt3673-2019/wikis/Project-Ideas#nfc-card-emulator) on the wiki.

The idea is to use your card as an emulator for an access card, such as the NTNU cards. You scan a card and store it, and then it can later be used as a replacement for the card.

The main motivation behind the idea was to use the NTNU cards, as such the app only supports reading of Mifare Classic 1K cards (which is the technology the NTNU cards use).
The app can recognice any NFC card, but will not do anything other than say the card type is not supported.

Testing this app requires a physical device with NFC (an emulator does not suffice) and an NTNU card (or other Mifare Classic 1K cards).


## Features of the app

NFC: Reading of Mifare Classic 1K cards

Localization: The user can change between English and Norwegian

Location services: The app stores (if permitted by the user) the location when a card is added.
When selecting a card to emulate or edit, the user can select to sort based on location. The app finds the users current location
and compares that to the location of the cards. The cards are then sorted based on your current location, the cards created closest to the current location are shown first.
The idea behind this is that one might have multiple cards for different places, and when selecting a card would probably want to see the closest first.

Storage is done with SQLite and Room.

Scanning a Mifare Classic card when the app is closed opens the scanning activity (still needs to be rescanned inside the app)



## How the app works

1. To scan a new card, click the "Scan" button in the start activity. If your device does not support NFC you will not be allowed to enter this activity.
2. Hold the card to your phone for 3-5 seconds (if you don't hold it long enough an error message will appear). When everything is read from the card you can continue to the next activity to store the card.
3. If this is the first time adding a card, a request for your location will appear. If you accept this request your location at the time of adding the card will be stored. Give your card a name and click "Add card".
4. Click on "Emulate". Here you can sort your cards based on time of creation, the cards name, or the location the card was added at relative to your current position (the closest cards come first).
5. Select a card by clicking on it and hold your phone towards a card reader (the emulating functionality doesn't work)
6. Cards can be edited by clicking the "Edit" button in the start activity. The same list as in the emulate activity will be shown, select an item and you can change the name of the card.
7. Language can be changed by clicking the flag button in the start activity (Norwegian and English support).


## Problems with emulating

Mifare is a proprietary technology and Mifare Classic doesn't seem to use NDEF messages to hold data.
We tried gathering the data by reading the blocks inside the sectors on the card, and we do get some data, but we haven't found a way to use this in a meaningful way.

As seen from [this](https://stackoverflow.com/questions/20055497/emulate-mifare-card-with-android-4-4) answer on StackOverflow, 
Mifare Classic only partially operates on ISO/IEC 14443-3 (which is a part of the 
[Android Host Card Emulation](https://developer.android.com/images/nfc/protocol-stack.png), HCE, stack), so it is not possible
to emulate Mifare Classic cards with HCE (this answer is from late 2013, but we haven't found anything else to make it seem like it's possible now).

Using HCE requires a resource file (see [app/src/main/res/xml/apduservice.xml](app/src/main/res/xml/apduservice.xml)) 
which includes AID (application ID) filters that decide what the app should recognize. 
The AID in that file is found [here](https://www.eftlab.com/index.php/site-map/knowledge-base/211-emv-aid-rid-pix) (search for "GOOGLE_MIFARE_MANAGER_AID"),
in hopes that it would work for Mifare Classic, but we were out of luck.

According to the first comment on [this](https://stackoverflow.com/questions/35935691/find-aid-of-nfc-reader)
question on StackOverflow (answered by the same person as the first question, this time in 2016),
Mifare Classic readers do not use AIDs (and that makes it not possible to use with HCE)


We have tried with both HCE and Peer-to-peer communication (Android Beam), but we haven't been able to emulate the cards.
The card readers recognize the phone (they probably recognize anything with NFC), but we haven't been able to make the phone detect it and therefore can't communicate with it.

