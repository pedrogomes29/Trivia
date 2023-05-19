# CPD Assignment 2  - Distributed Systems Assignment
## Simple Team Based Trivia Game
### Group Members
- Rui Pires (up202008252@fe.up.pt)
- João Reis (up202007227@fe.up.pt)
- Pedro Gomes (up202006322@up.pt)

## Project Description

- This project is a simple trivia game, where two teams, composed by two players face off to see who answers more questions correctly.
- There are five rounds and each round, two questions are asked, one to each team. They are chosen randomly from a list of questions.
- The game is played in a terminal, where the players can see the questions and answer them.
- If one of the players answers the question correctly, the other player from his team can't answer it. And the next question is asked to this team.
- Answering a question correctly gives the player who answered 1 point, and answering it incorrectly decreases the player's skill level by 1 point.
- A team wins the game if they answer more questions correctly than the other team, and all the players in the winning team increase their skill level by 10. The losing team decreases their skill level by 10.
- If the same number of questions are answered correctly by both teams, the game is a tie and no player increases their skill level.
- After the game is over and the skill level of the players is updated in the database, the players can choose to play again or exit the game.

## How to run the project

- To run the server you need to be in the out/production/assign2 folder and run the following command:
```
java server.Server
```
- To run the client you need to be in the out/production/assign2 folder and run the following command:
```
java client.Client
```
- If you want to try to play a game using just your pc, simply open more terminals and run the client in each one of them, and register more accounts.
- Be aware that in order to log in to a different account, if you are already logged in one terminal, you have to run the client with one command line argument, as follows:
```
java client.Client clear_cookies
```
- This will clear the cookies, and you will be able to log in to a different account.