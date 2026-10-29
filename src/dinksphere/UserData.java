/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dinksphere;
import java.util.ArrayList;

/**
 *
 * @author juliepearledoria
 */
public class UserData {
      public static ArrayList<UserAccount> accounts = new ArrayList<>();

    static {
        // Temporary test player account
        accounts.add(
            new UserAccount(
                "Test Player",
                "player",
                "player@dinksphere.com",
                "player123"
            )
        );
    }
}

