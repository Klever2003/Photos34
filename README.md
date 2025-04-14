Prerequisites

- Java JDK 21
- VS Code is preferred with Java Extension Pack 

Compiling the Program
- To compile the program use this command: 

javac -d bin --module-path "lib/javafx-sdk-21/lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp "src" src/photos/*.java src/photos/controller/*.java src/photos/model/*.java

Note: The FXML files are already included in the bin/photos/view directory, so you don't need to copy them.

Running the Program
- There are two ways to run the program:
Method 1: Using Command Prompt/PowerShell when in the Project Directory:

java --module-path "lib/javafx-sdk-21/lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp "bin" photos.Photos34

Method 2: Using VS Code Run Feature
- Open the Photos34.java file
- Click the "Run Java" button that appears above the main method
- VS Code will use the configuration in .vscode/launch.json to run the application