package hu.xannosz.blue.queen;


import hu.xannosz.microtools.Password;

public class App {
    public static void main(String[] args) throws Exception {
        Queen queen = new Queen();
        System.out.println("##:"+ Password.getSaltedHash("password"));
    }
}
