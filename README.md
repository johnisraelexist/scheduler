Scheduler
Instructions:
  1. We need to calculate calendar schedules for project plans
  2. Each project plan consists of tasks. Every task has a certain duration.
  3. A task can depend on zero or more other tasks. If a task depends on some other tasks, it can only be started after these tasks are completed
  4. So, for a set of tasks (with durations and dependencies), the solution for the challenge should generate a schedule, i.e. assign Start and End Dates for every task
  5. It is ok to have a console app
  6. The solution should be pushed to GitHub

Prerequisites
  1. Java Development Kit (JDK): Ensure you have a compatible JDK installed (Java 17). Check your version using java -version in your terminal. 
     Download and install the latest JDK from https://www.oracle.com/java/technologies/javase-downloads.html if needed.
  2. Integrated Development Environment (IDE): Choose your preferred IDE (e.g., IntelliJ IDEA, Eclipse).
  3. Maven - to use mvn command
  4. Git - to use git command
   
Setup to run application
  1. Clone the Repository:
     Open a terminal and navigate to your desired local directory. Clone the project from GitHub using the following command: git clone https://github.com/johnisraelexist/scheduler.git
  2. Navigate to your project directory, you can use an IDE to open your project and open a terminal there
  3. Build the project using maven. Use this command: mvn clean install
  4. Run the application. Use this command: mvn spring-boot:run
  5. The application will start using the H2 database. This app has a data initializer for testing purposes. If you don't need it for testing you can remove DataInitializer.class. 
     If you prefer to connect to a different database (e.g., MySQL, PostgreSQL), you can modify the application.properties file located in the src/main/resources directory. 
     Make sure you have the required database driver dependency included in your pom.xml.

API Endpoints
The following endpoints are available for managing project plans and tasks.
You can test these enpoints via postman, this is the postman collection: https://api.postman.com/collections/38882669-acd07b5f-d8ed-42d4-b7a7-75cacf12edf5?access_key=PMAT-01J9TD8AK51YNFDHN1ZVSJFZNF
Base URL: http://localhost:8080/api/projects
  1. Create a Project Plan with tasks
      Endpoint: /create
      Method: POST
      Description: Creates a new project plan.
      Sample Request Body:
        {
        "name": "New Banking Project",
        "projectStartDate": "2024-10-15",
        "tasks": [
          {
            "name": "Frontend Development",
            "duration": 10,
            "dependencies": [],
            "projectPlanId": null
          },
          {
            "name": "Backend Development",
            "duration": 15,
            "dependencies": [7],
            "projectPlanId": null
          },
          {
            "name": "Testing",
            "duration": 5,
            "dependencies": [8],
            "projectPlanId": null
          }
        ]
      }
      
      Sample Response:
      HTTP 200 OK
      Project plan created with ID: {projectId}

  2. Add a Task to a Project Plan
      Endpoint: /add-task
      Method: POST
      Description: Adds a task to an existing project plan.
      Sample Request Body:
      {
        "projectPlanId": 1,
        "name": "Task 1",
        "description": "Details about the task",
        "startDate": "2024-10-15",
        "endDate": "2024-11-15"
      }
      Sample Response:
      HTTP 200 OK
      Task added to project plan with ID: {projectPlanId}
     
  3. Retrieve All Project Plans
      Endpoint: /retrieve-all
      Method: GET
      Description: Fetches all project plans.
      No Request Body
      Response will be all the project plans
     
  4. Update a Task
      Endpoint: /tasks/{taskId}
      Method: PUT
      Description: Updates an existing task and recalculates affected dates.
      Path Variable: taskId - The ID of the task to update.
      Sample Request Body:
      {
        "name": "Updated Testing",
        "duration": 7,
        "dependencies": [6], 
        "projectPlanId": 2
       }
      Response:
      HTTP 200 OK
      Task updated and affected dates recalculated.
  
  5. Update a Project
      Endpoint: /{projectId}
      Method: PUT
      Description: Updates an existing project plan and recalculates affected dates.
      Path Variable: projectId - The ID of the project to update.
      Sample Request Body:
      {
        "name": "Updated Website Project",
        "projectStartDate": "2024-10-15"
      }
      Sample Response:
      HTTP 200 OK
      Project updated and affected dates recalculated.
  
  6. Delete a Task
      Endpoint: /tasks/{taskId}
      Method: DELETE
      Description: Deletes a specific task from a project plan.
      Path Variable: taskId - The ID of the task to delete.
      Sample Response:
      HTTP 200 OK
      Task deleted.

  8. Delete a Project
      Endpoint: /{projectId}
      Method: DELETE
      Description: Deletes a specific project plan.
      Path Variable:projectId - The ID of the project to delete.
      Sample Response:
      HTTP 200 OK
      Project deleted.

