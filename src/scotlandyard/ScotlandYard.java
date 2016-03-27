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
    private final List<Boolean> rounds;
    private final ScotlandYardGraph graph;
    private final List<PlayerData> players = new ArrayList<PlayerData>(); //holding the players as a list in proper order
    private final Map<Colour, PlayerData> playersMap = new HashMap<Colour, PlayerData>(); //holding the players as a key-value pair
    private int numberOfPlayersCurrentlyJoined = 0;
    private int lastKnownLocationOfMrX = 0;
    private int currentRound = 0;
    private int currentPlayerIndex = 0; //the index in the players list of the player whose turn is on
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
        whichPlayer.setLocation(move.target);
        whichPlayer.removeTicket(move.ticket);
        //if a detective does a move, we gotta pass the ticket onto Mr.X
        if(isPlayerADetective(move.colour))
        {
        	PlayerData mrX = getMrXData();
        	mrX.addTicket(move.ticket);
        }
        else
        {
        	//Mr.X makes a move, increment round and reveal him if necessary
        	incrementRound();
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
    	whichPlayer.removeTicket(Ticket.Double);
    	updateSpectators(move); //notifying all the spectators about the changed state of the game
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
        //set locations occupied by detectives as forbidden
        Set<Integer> forbiddenLocations = new HashSet<Integer>();
        for(PlayerData playerD : players)
        {
        	if(isPlayerADetective(playerD)) forbiddenLocations.add(playerD.getLocation());
        }
        //forbidden locations are set
    	List<MoveTicket> generatedMoves = graph.generateMoves(currentPlayer, currentPlayer.getLocation(), forbiddenLocations);
    	//now generate all the double moves for MrX
    	List<MoveDouble> doubleMoves = new ArrayList<MoveDouble>();
    	if(isPlayerMrX(currentPlayer) && playerHasATicketOfType(currentPlayer, Ticket.Double))
    	{
    		for(MoveTicket move : generatedMoves)
    		{
    			//backtracking all the double moves
    			currentPlayer.removeTicket(move.ticket);
    			List<MoveTicket> newMoves = graph.generateMoves(currentPlayer, move.target, forbiddenLocations);
    			currentPlayer.addTicket(move.ticket);
    			//now combining the tickets
    			for(MoveTicket move2 : newMoves)
    			{
    				MoveDouble doubleTicket = MoveDouble.instance(player, move, move2);
    				//add the double ticket only if we don't have such a combo already
    				if(!doubleMoves.contains(doubleTicket)) doubleMoves.add(doubleTicket);
    			}
    		}
    	}
    	List<Move> result = new ArrayList<Move>();
    	result.addAll(generatedMoves);
    	result.addAll(doubleMoves);
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
    	return validMoves(player.getColour());
    }

    /**
     * Allows spectators to join the game. They can only observe as if they
     * were a detective: only MrX's revealed locations can be seen.
     *
     * @param spectator the spectator that wants to be notified when a move is made.
     */
    public void spectate(Spectator spectator) {
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
        if(numberOfPlayersCurrentlyJoined > numberOfDetectives + 1) return false;
    	PlayerData newPlayer = new PlayerData(player, colour, location, tickets);
        //adding the Black player(Mr.X) as the first player
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
    	//if he's found or he cannot make a move
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
    		//if the game is over and Mr.X hasn't been found or he can move, then he must be the one winning
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
    	if(!isReady()) return false;
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
    		//if first move is not MovePass, then he can move
    		if(!(possibleMoves.get(0) instanceof MovePass))
    		{
    			aDetectiveCanMove = true;
    			break;
    		}
    	}
        if(!aDetectiveCanMove) return true;
        //now check if mr.X can move, if he cannot he has lost
        List<Move> mrXMoves = validMoves(getMrXColour());
        //if his moves list is empty, he cannot make a move
        if(mrXMoves.isEmpty()) return true;
        return false;
    }

    /**
     * A game is ready when all the required players have joined.
     *
     * @return true when the game is ready to be played, false otherwise.
     */
    public boolean isReady() {
        if(numberOfPlayersCurrentlyJoined != numberOfDetectives + 1) return false;
        if(!isPlayerMrX(players.get(0))) return false; //first player is not Mr.X
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
    
    private void updateSpectators(Move move) {
    	//handle Mr.X in a custom way to retain his secret location
    	if(move != null && isPlayerMrX(move.colour)) move = generateNewMoveWithDifferentTarget(move, getLastRevealedLocationOfMrX());
    	for(Spectator spectator : spectators) spectator.notify(move);
    }
    
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
    		moveTicket1 = MoveTicket.instance(moveTicket1.colour, moveTicket1.ticket, newTarget);
    		moveTicket2 = MoveTicket.instance(moveTicket2.colour, moveTicket2.ticket, newTarget);
    		toReturn = MoveDouble.instance(move.colour, moveTicket1, moveTicket2);
    	}
    	return toReturn;
    }
    
    /**
     * 
     */
    private void incrementRound() {
    	++currentRound;
    }
    
    /**
     * 
     * @param player
     * @return
     */
    private PlayerData getPlayerDataByColour(Colour player) {
    	return playersMap.get(player);
    }
    
    /**
     * 
     * @param player
     * @return
     */
    public boolean isPlayerMrX(Colour player) {
    	return player == mrXColour;
    }
    
    /**
     * 
     * @param player
     * @return
     */
    private boolean isPlayerMrX(PlayerData player) {
    	return isPlayerMrX(player.getColour());
    }
    
    /**
     * 
     * @param player
     * @return
     */
    public boolean isPlayerADetective(Colour player) {
    	return !isPlayerMrX(player);
    }
    
    /**
     * 
     * @param player
     * @return
     */
    private boolean isPlayerADetective(PlayerData player) {
    	return !isPlayerMrX(player);
    }
    
    /**
     * 
     * @param location
     */
    private void revealMrX() {
    	lastKnownLocationOfMrX = getMrXData().getLocation();
    }
    
    /**
     * 
     * @return
     */
    public int getLastRevealedLocationOfMrX() {
    	return lastKnownLocationOfMrX;
    }
    
    /**
     * 
     * @param player
     * @param ticket
     * @return
     */
    public boolean playerHasATicketOfType(Colour player, Ticket ticket) {
    	return getPlayerTickets(player, ticket) > 0;
    }
    
    /**
     * 
     * @param player
     * @param ticket
     * @return
     */
    private boolean playerHasATicketOfType(PlayerData player, Ticket ticket) {
    	return playerHasATicketOfType(player.getColour(), ticket);
    }
    
    /**
     * 
     * @param player
     * @return
     */
    public boolean playerHasJoinedGame(Colour player)
    {
    	return playersMap.containsKey(player);
    }
    
    /**
     * 
     * @param player1
     * @param player2
     * @return
     */
    private boolean playersOverlap(Colour player1, Colour player2) {
    	PlayerData player1Data = getPlayerDataByColour(player1);
    	PlayerData player2Data = getPlayerDataByColour(player2);
    	return playersOverlap(player1Data, player2Data);
    }
    
    /**
     * 
     * @param player1
     * @param player2
     * @return
     */
    private boolean playersOverlap(PlayerData player1, PlayerData player2) {
    	return player1.getLocation() == player2.getLocation();
    }
    
    /**
     * 
     * @return
     */
    private PlayerData getMrXData()
    {
    	return getPlayerDataByColour(getMrXColour());
    }
    
    /**
     * 
     * @return
     */
    public Colour getMrXColour() {
    	return mrXColour;
    }
    
    /**
     * 
     * @return
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
