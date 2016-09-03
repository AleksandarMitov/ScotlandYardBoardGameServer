# Scotland Yard Board Game Judge:
Welcome to the Scotland Yard project! During the next few weeks, we will develop a Java application (with GUI) that allows you to play Scotland Yard. We have provided a skeleton for the project along with a basic GUI, it is your job to implement the game model and improve the GUI.

We will implement the full version of the game, not the beginners version. We will also not model the Police and when a detective moves, the ticket they have used will be given to Mr X.

The project is structured in a very similar way to the Draughts game you implemented, the folders are:

* src: Contains the `.java` source files. You will be working in `scotlandyard/ScotlandYard.java` for the first part.
* resources: Contains all of the resources for the provided GUI.
* doc: Contains the Javadocs for the project.
* lib: Contains the libraries we need for the project.
* ant: Contains Ant binaries.
* tests: Contains the JUnit tests for this project.
* test-resources: Contains some different graphs to help test the project.

As with all of the previous coursework, we have provided a build file alongside ant and an extensive test suite implemented in JUnit. To test the model, you should run:
```
./ant/bin/ant test
```
If your interested in what Ant is doing here, take a look in `build.xml`. Your first run of the test suite will produce a lot of failures since you have not yet implemented the different stages of the coursework, so don't freak out!

Unlike most of the previous coursework, we are leaving a lot of the implementation details up to you. You should choose the data structures that make most sense to you and make new classes and such as and when you feel they are necessary.

Refer to the Javadocs in the `doc` directory if you are unsure as to what any methods do.

## Part 1

Your first task is to implement the various methods that initialise a game. A good place to start is the body of the constructor in `src/scotlandyard/ScotlandYard.java`. This should be used to set the game up in the configuration specified by the constructor arguments. Once done, you will need to implement the `join` method. This is the means by which a `Player` can join the game, and so you must store the information passed in. Note that you should consult the Javadoc and the testing files to get additional information about the required functionality of methods. Once this has been done, you will want to implement the `isReady` method. This method should return true if the required number of players has joined and the game is ready to be played. Once these are done, you will also want to implement the methods `getPlayerTickets`, `getPlayerLocation`, `getRounds` and `getPlayers`. When the `getPlayerLocation` method is used for Mr X, it should always return his last know location, which should be updated every time that Mr X reveals his position. A finished implementation of this stage should pass the `test-part-1` tests.


****

**TODO:** Implement the `ScotlandYard` constructor along with the
`join`, `isReady` `getPlayerTickets`, `getPlayerLocation`, `getRounds`
and `getPlayers` methods so you pass the `test-part-1` tests. You can
run them like so:
```
./ant/bin/ant test-part-1
```
You may also have to modify other methods in the class: there are
*TODO* notes scattered around where things have been left incomplete.

****


## Part 2

The next stage will focus on the means by which players can make moves
and the game interpret the moves. We have included the same Graph
library as was in the last coursework about Prim's and Dijkstra's
algorithms, the Graph passed into the `ScotlandYard` constructor uses
this library. You should use this Graph when determining which moves a
player can make. We pass a secret token (which is just a randomly
generated Integer, not so secret anymore haha) into the notify method
of a `Player`, this allows us to make sure the correct `Player` is
playing a move. The mechanics of the game work as follows:

1. At the start of a turn, the model produces a list of valid moves
   for the current player using the `validMoves` method.
2. The `notifyPlayer` method then notifies the player with a secret
   token, the list of valid moves, the player's current location on
   the map and a `Receiver` (this is implemented by the model).
3. The player then calls the `playMove` method of the `Receiver` with
   the chosen move and the secret token given to it.
4. The `playMove` method then checks that the token is correct. If it
   is, the move is then played using the `play` method which changes
   the state of the game.
5. The current player is then updated using the `nextPlayer` method.

The complicated part of this task is to correctly implement the
`validMoves` method. Take some time to look at the provided `Move`
classes to see how they relate to each other. Also make sure you
understand the process of finding valid moves and applying moves to
the state of the game. A finished implementation at this stage will
pass the `test-part-2-1`, `test-part-2-2` and `test-part-2-3` tests.


***

**TODO:** Implement the `notifyPlayer`, `getCurrentPlayer` and `nextPlayer` methods so you pass the `test-part-2-1` tests. You can run them like so:
```
./ant/bin/ant test-part-2-1
```

***

**TODO:** Implement the `validMoves` method so you pass the `test-part-2-2` tests. You can run them like so:
```
./ant/bin/ant test-part-2-2
```

***

**TODO:** Implement the `play` methods so you pass the `test-part-2-3` tests. You can run them like so:
```
./ant/bin/ant test-part-2-3
```

***


## Part 3

For this part you will implement the functionality that enables spectators to spectate a game. A `Spectator` is notified of all game changes (when moves are made). The Observer pattern should be used to implement this. As each move is played by the Players the moves should be sent to all of the (registered) spectators. However, Mr X's moves are dealt with in a specific way so as not to broadcast his actual locations except for the rounds when Mr X is visible. A finished implementation of this stage should pass the `test-part-3-1` and `test-part-3-2` tests:


***

**TODO:** Implement the `spectate` method and notify `Spectator`s when moves are made so you pass the `test-part-3-1` tests. You can run them like so:
```
./ant/bin/ant test-part-3-1
```

***


Now, implement the round counting for the game. Each time Mr X makes a single move the round counter should be increased by one. Pay attention to double moves where the round counter increases twice, once for each of the two moves.


***

**TODO:** Implement round counting and the `getRound` method so you pass the `test-part-3-2` tests. You can run them like so:
```
./ant/bin/ant test-part-3-2
```

***


## Part 4

For the final part you will need to make sure that the `isGameOver` and `getWinningPlayers` methods are working. Consult the rules of the game to find out the winning conditions. If Mr X has won, the `getWinningPlayers` method should return a set containing only Mr X's colour. If the detectives have won it should return a set containing all the detective's colours. If the game is not over, the returned set should be empty.


***

**TODO:** Implement the `isGameOver` and `getWinningPlayers` methods so you pass the `test-part-4` tests. You can run them like so:
```
./ant/bin/ant test-part-4
```

***


Hopefully you are now passing all of the tests! Okay, remember we said that we provided a GUI? Well, now's your chance to use it! Run the following to start the GUI:
```
./ant/bin/ant gui
```

***

**TODO:** Play some games!

***
