# Solution Implementation for Long Rrunning HTTP Backends

## Overview
This project implements a scalable backend solution for handling long-running tasks between a React-based client and a Spring Boot backend.  
It leverages message queues, caching, and WebSockets to ensure real-time updates, fault tolerance, and efficient resource utilization.

## Services Overview
- **Spring Boot Application** containing both Publisher and Consumer services.
- **React Client** for submitting tasks and receiving status updates.

## Tech Stack
- **Backend:** Spring Boot, RabbitMQ, Redis, PostgreSQL, WebSockets
- **Frontend:** React
- **Containerization:** Docker, Docker Compose


## Application Architecture
![Application Architecture](assets/architecture%20diagram.svg)
