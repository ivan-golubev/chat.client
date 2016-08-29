Chat client
------------------------------------

Installation
-------------------------
The same steps as described in the chat.server project – so configure the [chat.server][1] first.


Usage
-----
To build the project:
execute this in console (cd to the project root directory first) or in you favourite IDE:

`> gradle clean build`

To run the app execute this in the terminal:

`> cd build\libs\`

`> java -jar chat.client-1.0.jar`

Extending the system with new message types (commands)
-----------------------------------------------------
Go to the [chat.common][5] project for more details.

[1]: https://github.com/ivan-golubev/chat.server