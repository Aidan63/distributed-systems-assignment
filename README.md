# distributed-systems-assignment

Projects created for university distributed systems module. End goal of the assignment was to create a real time multiplayer racing game.
Module required that the assignment was split into three parts.

### Part 1
Basic window which displays a vehicle and is able to be rotated, change image, colour, ect.

### Part 2
Local multiplayer racing game. Features up to 4 player split screen multiplayer with vehicle customisation. Game features single race track using SAT2D for precise collision detection and a quadtree for fast AABB collisions.

### Part 3
Same game as part 2 but now over the internet instead of split screen. Multiplayer was implemented entirely over UDP due to speed being more important than reliability for the majority of data.
A Quake style snapshot and reliability layer was built on top of UDP to handle game packets. The games server is authoratative and runs headless with the same game simulation as the client. This allows the clients to implement client side prediction so they don't have to wait for a server.
