package scotlandyard;

import java.io.IOException;
import java.util.*;

/**
 * A class to perform all of the game logic.
 */

public class ScotlandYard implements ScotlandYardView, Receiver {

    protected MapQueue<Integer, Token> queue;
    protected int gameId;
    protected Random random;
    private final int numberOfDetectives;
    /**
     * a list holding a value true if mr.X has to reveal himself
     * in that round, contains false otherwise.
     */
    private final List<Boolean> rounds;
    /**
     * Contains the graph representing the game map
     */
    private final ScotlandYardGraph graph;
    /**
     * Hold a list of players in their order of playing the game.
     * Mr.X should always be the first one making a move.
     */
    private final List<PlayerData> players = new ArrayList<PlayerData>(); //holding the players as a list in proper order
    /**
     * A Map mapping a player with a given {@link scotlandyard.Colour} to his {@link scotlandyard.PlayerData}
     */
    private final Map<Colour, PlayerData> playersMap = new HashMap<Colour, PlayerData>(); //holding the players as a key-value pair
    /**
     * Used by the {@link scotlandyard.ScotlandYard#isReady()} method to make sure all players have
     * joined the game before its start
     */
    private int numberOfPlayersCurrentlyJoined = 0;
    /**
     * Contains the last revealed location of Mr.X
     */
    private int lastKnownLocationOfMrX = 0;
    private int currentRound = 0;
    /**
     * The index in the players list of the player whose turn is on
     */
    private int currentPlayerIndex = 0;
    /**
     * A list holding all the spectators in the game.
     * Part of the implementation of the Observer design pattern.
     */
    private final List<Spectator> spectators = new ArrayList<Spectator>();
    private final Colour mrXColour = Colour.Black;
    /**
     * Constructs a new ScotlandYard object. This is used to perform all of the game logic.
     *
     * @param numberOfDetectives the number of detectives in the game.
     * @param rounds the List of booleans determining at which rounds Mr X is visible.
     * @param graph the graph used to represent the board.
     * @param queue the Queue used to put pending moves onto.
     * @param gameId the id of this game.
     */
    public ScotlandYard(Integer numberOfDetectives, List<Boolean> rounds, ScotlandYardGraph graph, MapQueue<Integer, Token> queue, Integer gameId) {
        this.queue = queue;
        this.gameId = gameId;
        this.random = new Random();
        this.numberOfDetectives = numberOfDetectives;
        this.rounds = rounds;
        this.graph = graph;
    }

    /**
     * Starts playing the game.
     */
    public void startRound() {
        if (isReady() && !isGameOver()) {
            turn();
        }
    }

    /**
     * Notifies a player when it is their turn to play.
     */
    public void turn() {
        Integer token = getSecretToken();
        queue.put(gameId, new Token(token, getCurrentPlayer(), System.currentTimeMillis()));
        notifyPlayer(getCurrentPlayer(), token);
    }

    /**
     * Plays a move sent from a player.
     *
     * @param move the move chosen by the player.
     * @param token the secret token which makes sure the correct player is making the move.
     */
    public void playMove(Move move, Integer token) {
        Token secretToken = queue.get(gameId);
        if (secretToken != null && token == secretToken.getToken()) {
            queue.remove(gameId);
            play(move);
            nextPlayer();
            startRound();
        }
    }

    /**
     * Returns a random integer. This is used to make sure the correct player
     * plays the move.
     * @return a random integer.
     */
    private Integer getSecretToken() {
        return random.nextInt();
    }

    /**
     * Notifies a player with the correct list of valid moves.
     *
     * @param colour the colour of the player to be notified.
     * @param token the secret token for the move.
     */
    private void notifyPlayer(Colour colour, Integer token) {
        PlayerData currentPlayer = getPlayerDataByColour(colour);
        currentPlayer.getPlayer().notify(currentPlayer.getLocation(), validMoves(colour), token, this);
    }

    /**
     * Passes priority onto the next player whose turn it is to play.
     */
    protected void nextPlayer() {
    	//we increment the index of the next player in the players list, also going
    	//back at the beginning if we've reached the end
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    /**
     * Allows the game to play a given move.
     *
     * @param move the move that is to be played.
     */
    protected void play(Move move) {
        if (move instanceof MoveTicket) play((MoveTicket) move);
        else if (move instanceof MoveDouble) play((MoveDouble) move);
        else if (move instanceof MovePass) play((MovePass) move);
    }

    /**
     * Plays a MoveTicket.
     *
     * @param move the MoveTicket to play.
     */
    protected void play(MoveTicket move) {
    	PlayerData whichPlayer = getPlayerDataByColour(move.colour);
        whichPlayer.setLocation(move.target); //setting the player to the new location
        whichPlayer.removeTicket(move.ticket); //removing the ticket from his ticket pool
        //if a detective does a move, we gotta pass the ticket onto Mr.X
        if(isPlayerADetective(move.colour))
        {
        	PlayerData mrX = getMrXData();
        	//giving the ticket to Mr.X
        	mrX.addTicket(move.ticket);
        }
        else
        {
        	//Mr.X makes a move, increment round and reveal him if necessary
        	incrementRound();
        	//reveal Mr.X if we're supposed to in this round
        	if(rounds.get(currentRound)) revealMrX();
        }
        updateSpectators(move); //notifying all the spectators about the changed state of the game
    }

    /**
     * Plays a MoveDouble.
     *
     * @param move the MoveDouble to play.
     */
    protected void play(MoveDouble move) {
    	PlayerData whichPlayer = getPlayerDataByColour(move.colour);
    	//removing the double ticket from the player's ticket pool
    	whichPlayer.removeTicket(Ticket.Double);
    	updateSpectators(move); //notifying all the spectators about the changed state of the game
    	//playing the MoveTickets individually
    	play((Move) move.move1);
    	play((Move) move.move2);
    }

    /**
     * Plays a MovePass.
     *
     * @param move the MovePass to play.
     */
    protected void play(MovePass move) {
        /*
         In a MovePass move, we basically stand still in the same position
         so we don't really have to do anything here
         */
    	updateSpectators(move); //notifying all the spectators about the changed state of the game
    }

    /**
     * Returns the list of valid moves for a given player.
     *
     * @param player the player whose moves we want to see.
     * @return the list of valid moves for a given player.
     */
    public List<Move> validMoves(Colour player) {
    	PlayerData currentPlayer = getPlayerDataByColour(player);
        //set locations occupied by the detectives as forbidden
        Set<Integer> forbiddenLocations = new HashSet<Integer>();
        for(PlayerData playerD : players)
        {
        	//if the player is a detective, add his current location as forbidden 
        	if(isPlayerADetective(playerD)) forbiddenLocations.add(playerD.getLocation());
        }
        //forbidden locations are set
    	List<MoveTicket> generatedMoves = graph.generateMoves(currentPlayer, currentPlayer.getLocation(), forbiddenLocations);
    	//now generate all the double moves for Mr.X
    	List<MoveDouble> doubleMoves = new ArrayList<MoveDouble>();
    	//if the player is Mr.X and he has DoubleTickets left
    	if(isPlayerMrX(currentPlayer) && playerHasATicketOfType(currentPlayer, Ticket.Double))
    	{
    		//generate every combination of pairs of moves
    		for(MoveTicket move : generatedMoves)
    		{
    			//backtracking all the double moves
    			//removing the ticket for the first move
    			currentPlayer.removeTicket(move.ticket);
    			//generating the possibilities for the second move
    			List<MoveTicket> newMoves = graph.generateMoves(currentPlayer, move.target, forbiddenLocations);
    			//adding the ticket for the first move back
    			currentPlayer.addTicket(move.ticket);
    			//now generating the pairs
    			for(MoveTicket move2 : newMoves)
    			{
    				MoveDouble doubleTicket = MoveDouble.instance(player, move, move2);
    				//add the double ticket as a result only if we don't have such a combo already
    				if(!doubleMoves.contains(doubleTicket)) doubleMoves.add(doubleTicket);
    			}
    		}
    	}
    	List<Move> result = new ArrayList<Move>();
    	//merging the single and double moves
    	result.addAll(generatedMoves);
    	result.addAll(doubleMoves);
    	//if we have no legal moves and are dealing with a grumpy detective, we gotta return a MovePass move
    	if(result.isEmpty() && isPlayerADetective(player)) result.add(MovePass.instance(player));
    	return result;
    }
    
    /**
     * Returns the list of valid moves for a given player.
     *
     * @param player the player whose moves we want to see.
     * @return the list of valid moves for a given player.
     */
    public List<Move> validMoves(PlayerData player) {
    	//making use of polymorphism and stuff
    	return validMoves(player.getColour());
    }

    /**
     * Allows spectators to join the game. They can only observe as if they
     * were a detective: only MrX's revealed locations can be seen.
     *
     * @param spectator the spectator that wants to be notified when a move is made.
     */
    public void spectate(Spectator spectator) {
        //adding the spectator to the spectators list as part of the implementation fo the Observer design pattern
    	spectators.add(spectator);
    }
    
    /**
     * Unregisters a spectator from the spectator list.
     * 
     * @param spectator the spectator that wants to stop being notified on state changes
     */
    public void unregisterSpectator(Spectator spectator)
    {
    	spectators.remove(spectator);
    }
    
    /**
     * Allows players to join the game with a given starting state. When the
     * last player has joined, the game must ensure that the first player to play is Mr X.
     *
     * @param player the player that wants to be notified when he must make moves.
     * @param colour the colour of the player.
     * @param location the starting location of the player.
     * @param tickets the starting tickets for that player.
     * @return true if the player has joined successfully.
     */
    public boolean join(Player player, Colour colour, int location, Map<Ticket, Integer> tickets) {
    	//check if too many players are trying to join the game
        if(numberOfPlayersCurrentlyJoined > numberOfDetectives) return false;
    	PlayerData newPlayer = new PlayerData(player, colour, location, tickets);
        //adding the Black player(Mr.X) as the first player at the beginning of the players list
    	if(isPlayerMrX(colour)) players.add(0, newPlayer);
        else players.add(newPlayer);
    	playersMap.put(colour, newPlayer); //also adding the player in the players map
    	++numberOfPlayersCurrentlyJoined;
        return true;
    }

    /**
     * A list of the colours of players who are playing the game in the initial order of play.
     * The length of this list should be the number of players that are playing,
     * the first element should be Colour.Black, since Mr X always starts.
     *
     * @return The list of players.
     */
    public List<Colour> getPlayers() {
        List<Colour> playerColours = new ArrayList<Colour>();
        for(PlayerData player : players) playerColours.add(player.getColour());
        return playerColours;
    }

    /**
     * Returns the colours of the winning players. If Mr X it should contain a single
     * colour, else it should send the list of detective colours
     *
     * @return A set containing the colours of the winning players
     */
    public Set<Colour> getWinningPlayers() {
    	Set<Colour> result = new HashSet<Colour>();
    	if(!isGameOver()) return result;
    	//check if MrX has been found
    	PlayerData mrX = getMrXData();
    	boolean mrXHasBeenFound = false;
    	for(PlayerData player : players)
    	{
    		//check if a detective is on the same location as Mr.X
    		if(isPlayerADetective(player) && playersOverlap(player, mrX))
    		{
    			mrXHasBeenFound = true;
    			break;
    		}
    	}
    	//if Mr.X's found or he cannot make a move
    	if(mrXHasBeenFound || validMoves(mrX).isEmpty())
    	{
    		//detectives win
    		for(PlayerData player : players)
    		{
    			if(isPlayerADetective(player)) result.add(player.getColour());
    		}
    	}
    	else
    	{
    		//if the game is over and Mr.X hasn't been found or he can still move, then he must be the one winning
    		result.add(mrX.getColour());
    	}
    	return result;
    }

    /**
     * The location of a player with a given colour in its last known location.
     *
     * @param colour The colour of the player whose location is requested.
     * @return The location of the player whose location is requested.
     * If Black, then this returns 0 if MrX has never been revealed,
     * otherwise returns the location of MrX in his last known location.
     * MrX is revealed in round n when {@code rounds.get(n)} is true.
     */
    public int getPlayerLocation(Colour colour) {
    	//because Mr.X is special
    	if(isPlayerMrX(colour))
    	{
    		if(rounds.get(currentRound)) revealMrX();
    		else return getLastRevealedLocationOfMrX();
    	}
    	return getPlayerDataByColour(colour).getLocation();
    }

    /**
     * The number of a particular ticket that a player with a specified colour has.
     *
     * @param colour The colour of the player whose tickets are requested.
     * @param ticket The type of tickets that is being requested.
     * @return The number of tickets of the given player.
     */
    public int getPlayerTickets(Colour colour, Ticket ticket) {
    	Integer ticketCount = getPlayerDataByColour(colour).getTickets().get(ticket);
    	return ticketCount != null ? ticketCount : 0;
    }

    /**
     * The game is over when MrX has been found or the agents are out of
     * tickets. See the rules for other conditions.
     *
     * @return true when the game is over, false otherwise.
     */
    public boolean isGameOver() {
    	//if the game isn't even ready it cannot be over
    	if(!isReady()) return false;
    	//if it's ready but there are no detectives, then it's over
    	if(isReady() && players.size() < 2) return true;
    	//check if MrX has been found
    	PlayerData mrX = getMrXData();
    	for(PlayerData player : players)
    	{
    		//check if a detective is on the same location as Mr.X
    		if(isPlayerADetective(player) && playersOverlap(player, mrX)) return true;
    	}
    	//now check if we're past 22nd round
    	//if we're at the last round and it's mr.X's turn again, we'd have to go to a round past the last one 
    	if(getCurrentPlayer() == getMrXColour() && getRound() == getRounds().size()-1) return true;
    	//now check if all the detectives cannot move
    	boolean aDetectiveCanMove = false;
    	for(PlayerData player : players)
    	{
    		if(isPlayerMrX(player)) continue; //skip Mr.X
    		List<Move> possibleMoves = validMoves(player); //generate the possible moves for current detective
    		//if first move is not MovePass, then he can definitely make a move
    		if(!(possibleMoves.get(0) instanceof MovePass))
    		{
    			aDetectiveCanMove = true;
    			break;
    		}
    	}
    	//if no detective can move, Mr.X turns out to be one lucky guy
        if(!aDetectiveCanMove) return true;
        //now check if mr.X can move, if he cannot he has lost
        List<Move> mrXMoves = validMoves(getMrXColour());
        //if his moves list is empty, he cannot make a move and he better get ready to have a good time with the inmates
        if(mrXMoves.isEmpty()) return true;
        return false;
    }

    /**
     * A game is ready when all the required players have joined.
     *
     * @return true when the game is ready to be played, false otherwise.
     */
    public boolean isReady() {
    	//the the number of joined player is less than the supposed amount, the show's not ready to start yet
        if(numberOfPlayersCurrentlyJoined != numberOfDetectives + 1) return false;
        if(!isPlayerMrX(players.get(0))) return false; //first player is not Mr.X, we can't afford to lose our star
        return true;
    }

    /**
     * The player whose turn it is.
     *
     * @return The colour of the current player.
     */
    public Colour getCurrentPlayer() {
        return players.get(currentPlayerIndex).getColour();
    }

    /**
     * The round number is determined by the number of moves MrX has played.
     * Initially this value is 0, and is incremented for each move MrX makes.
     * A double move counts as two moves.
     *
     * @return the number of moves MrX has played.
     */
    public int getRound() {
        return currentRound;
    }

    /**
     * A list whose length-1 is the maximum number of moves that MrX can play in a game.
     * The getRounds().get(n) is true when MrX reveals the target location of move n,
     * and is false otherwise.
     * Thus, if getRounds().get(0) is true, then the starting location of MrX is revealed.
     *
     * @return a list of booleans that indicate the turns where MrX reveals himself.
     */
    public List<Boolean> getRounds() {
    	return rounds;
    }
    
    /**
     * Updates all the spectators of the game with the move made
     * Handles Mr.X differently than the detectives, cause he's special like that
     * 
     * @param move the move that's made
     */
    private void updateSpectators(Move move) {
    	//handle Mr.X in a custom way to retain his secret location
    	if(move != null && isPlayerMrX(move.colour)) move = generateNewMoveWithDifferentTarget(move, getLastRevealedLocationOfMrX());
    	for(Spectator spectator : spectators) spectator.notify(move);
    }
    
    /**
     * Generates a similar Move, just with a different end target
     * 
     * @param move
     * @param newTarget
     * @return move with the given target
     */
    private Move generateNewMoveWithDifferentTarget(Move move, Integer newTarget) {
    	Move toReturn = move;
    	if(move instanceof MoveTicket)
    	{
    		toReturn = MoveTicket.instance(move.colour, ((MoveTicket) move).ticket, newTarget);
    	}
    	else if(move instanceof MoveDouble)
    	{
    		MoveTicket moveTicket1 = ((MoveDouble) move).move1;
    		MoveTicket moveTicket2 = ((MoveDouble) move).move2;
    		//modify each of the individual tickets
    		moveTicket1 = MoveTicket.instance(moveTicket1.colour, moveTicket1.ticket, newTarget);
    		moveTicket2 = MoveTicket.instance(moveTicket2.colour, moveTicket2.ticket, newTarget);
    		//now assemble them back into a double ticket. Phew
    		toReturn = MoveDouble.instance(move.colour, moveTicket1, moveTicket2);
    	}
    	return toReturn;
    }
    
    /**
     * Increments the game round
     */
    private void incrementRound() {
    	++currentRound;
    }
    
    /**
     * Returns a {@link scotlandyard.PlayerData} instance of the given player
     * @param player the player whose PlayerData we want
     * @return the PlayerData instance of the given player
     */
    private PlayerData getPlayerDataByColour(Colour player) {
    	//we use the PlayersMap as the main store
    	return playersMap.get(player);
    }
    
    /**
     * Tells if the given player is Mr.X
     * 
     * @param player the player we're interested in
     * @return true if he's Mr.X, false otherwise
     */
    public boolean isPlayerMrX(Colour player) {
    	return player == mrXColour;
    }
    
    /**
     * Tells if the given player is Mr.X
     * 
     * @param player the player we're interested in
     * @return true if he's Mr.X, false otherwise
     */
    private boolean isPlayerMrX(PlayerData player) {
    	//polymorphism in practice
    	return isPlayerMrX(player.getColour());
    }
    
    /**
     * Tells if the given player is a detective
     * 
     * @param player the player we're interested in
     * @return true if the player is a detective, false otherwise
     */
    public boolean isPlayerADetective(Colour player) {
    	//I don't like to repeat myself. Duhh
    	return !isPlayerMrX(player);
    }
    
    /**
     * Tells if the given player is a detective
     * 
     * @param player the player we're interested in
     * @return true if the player is a detective, false otherwise
     */
    private boolean isPlayerADetective(PlayerData player) {
    	//practicing what I preach baby
    	return !isPlayerMrX(player);
    }
    
    /**
     * Updates Mr.X's last known location
     */
    private void revealMrX() {
    	lastKnownLocationOfMrX = getMrXData().getLocation();
    }
    
    /**
     * Returns Mr.X's last revealed location
     * @return the last location Mr.X was kind enough to reveal
     */
    public int getLastRevealedLocationOfMrX() {
    	return lastKnownLocationOfMrX;
    }
    
    /**
     * Checks if a player has a ticket of a specific type
     * 
     * @param player the player whose tickets we're going through
     * @param ticket the ticket type we're looking for
     * @return true, if he has at least 1 ticket of the given type, false otherwise
     */
    public boolean playerHasATicketOfType(Colour player, Ticket ticket) {
    	return getPlayerTickets(player, ticket) > 0;
    }
    
    /**
     * Checks if a player has a ticket of a specific type
     * 
     * @param player the player whose tickets we're going through
     * @param ticket the ticket type we're looking for
     * @return true, if he has at least 1 ticket of the given type, false otherwise
     */
    private boolean playerHasATicketOfType(PlayerData player, Ticket ticket) {
    	//because polymorphism
    	return playerHasATicketOfType(player.getColour(), ticket);
    }
    
    /**
     * Tells if a player with a given colour is late to the party
     * 
     * @param player the player who we're interested in
     * @return Feeling DRY with this one, look above
     */
    public boolean playerHasJoinedGame(Colour player)
    {
    	return playersMap.containsKey(player);
    }
    
    /**
     * Tells if two given players collide on the same location
     * @param player1 the first player
     * @param player2 the secpmd player
     * @return true if they're on the same location, false otherwise
     */
    private boolean playersOverlap(Colour player1, Colour player2) {
    	PlayerData player1Data = getPlayerDataByColour(player1);
    	PlayerData player2Data = getPlayerDataByColour(player2);
    	//conventions can be a b...*cough* nevermind
    	return playersOverlap(player1Data, player2Data);
    }
    
    /**
     * Tells if two given players collide on the same location
     * @param player1 the first player
     * @param player2 the second player
     * @return true if they're on the same location, false otherwise
     */
    private boolean playersOverlap(PlayerData player1, PlayerData player2) {
    	return player1.getLocation() == player2.getLocation();
    }
    
    /**
     * Return an {@link scotlandyard.PlayerData} instance of Mr.X
     * @return Mr.X's player data
     */
    private PlayerData getMrXData()
    {
    	return getPlayerDataByColour(getMrXColour());
    }
    
    /**
     * Returns the {@link scotlandyard.Colour} of Mr.X
     * @return Mr.X's colour
     */
    public Colour getMrXColour() {
    	return mrXColour;
    }
    
    /**
     * Returns the detective's as a list of their {@link scotlandyard.PlayerData}
     * @return list of the detectives
     */
    private List<PlayerData> getDetectivesData() {
    	List<PlayerData> result = new ArrayList<PlayerData>();
    	for(PlayerData player : players)
    	{
    		if(!isPlayerMrX(player)) result.add(player);
    	}
    	return result;
    }
    
    
}
