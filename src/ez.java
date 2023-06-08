//package src;
//
//import java.io.File;
//import java.io.OutputStream;
//import java.net.InetAddress;
//import java.net.Socket;
//import java.util.ArrayList;
//
//public class ez {
//
//    <<<<<<HEAD
//    //On attend la connexion du client et on crée le clientSocket
//    Socket clientSocket = serverSocket.accept();
//
//    //On récupère ensuite le chemin d'accès du fichier root
//    rec = r.readLine();
//    rec = rec.replace("\t<root>", "");
//    rec = rec.replace("</root>", "");
//    String fileName = rec;
//
//    //on veut vérifier que le fichier root existe donc on va vérifier que fileName est un dossier
//    File root = new File(fileName);
//            if (!root.isDirectory()) {
//        OutputStream versClient = clientSocket.getOutputStream();
//        erreurChemin(versClient);
//    }
//
//
//    //La prochaine étape est de vérifier si l'adresse réseau du client est accepté ou non
//    //Pour cela on va créer une liste d'adresses acceptées et d'IP refusé
//    rec = r.readLine();
//    ArrayList<String> AdressesAcceptees = new ArrayList<>();
//    //On crée une boucle tant que les lignes que l'on lit de webconf contient accept
//            while (rec.contains("accept")) {
//        rec = rec.replace("\t<accept>", "");
//        rec = rec.replace("</accept>", "");
//        //On calcule le masque
//        String masque = calculerMasque(rec);
//        //On ajoute dans cet ordre l'adresse réseau puis le masque de cette adresse
//        AdressesAcceptees.add(rec);
//        AdressesAcceptees.add(masque);
//        //On n'oublie pas de lire la ligne suivante
//        rec = r.readLine();
//    }
//    //On passe à la liste des adresses refusées en suivant la même logique
//    ArrayList<String> AdressesRefusees = new ArrayList<>();
//            while (rec.contains("reject")) {
//        rec = rec.replace("\t<reject>", "");
//        rec = rec.replace("</reject>", "");
//        //On calcule le masque
//        String masque = calculerMasque(rec);
//        //On ajoute dans cet ordre l'adresse réseau puis le masque de cette adresse
//        AdressesRefusees.add(rec);
//        AdressesRefusees.add(masque);
//        //On n'oublie pas de lire la ligne suivante
//        rec = r.readLine();
//    }
//
//
//
//
//    //Ici, on va récupérer l'adresse IP local
//    InetAddress inetAddressLocalhost = InetAddress.getLocalHost();
//    String adresseIP = inetAddressLocalhost.getHostAddress();
//
//            System.out.println("Nouvelle connexion entrante : " + clientSocket.getInetAddress());
//
//=======
//
//    //On récupère ensuite le chemin d'accès du fichier root
//    rec = r.readLine();
//    rec = rec.replace("\t<root>", "");
//    rec = rec.replace("</root>", "");
//    String fileName = rec;
//
//    //La prochaine étape est de vérifier si l'adresse réseau du client est accepté ou non
//    //Pour cela on va créer une liste d'adresses acceptées et d'IP refusé
//    rec = r.readLine();
//    ArrayList<String> AdressesAcceptees = new ArrayList<>();
//    //On crée une boucle tant que les lignes que l'on lit de webconf contient accept
//            while (rec.contains("accept")) {
//        rec = rec.replace("\t<accept>", "");
//        rec = rec.replace("</accept>", "");
//        //On calcule le masque
//        String masque = calculerMasque(rec);
//        //On ajoute dans cet ordre l'adresse réseau puis le masque de cette adresse
//        AdressesAcceptees.add(rec);
//        AdressesAcceptees.add(masque);
//        //On n'oublie pas de lire la ligne suivante
//        rec = r.readLine();
//    }
//    //On passe à la liste des adresses refusées en suivant la même logique
//    ArrayList<String> AdressesRefusees = new ArrayList<>();
//            while (rec.contains("reject")) {
//        rec = rec.replace("\t<reject>", "");
//        rec = rec.replace("</reject>", "");
//        //On calcule le masque
//        String masque = calculerMasque(rec);
//        //On ajoute dans cet ordre l'adresse réseau puis le masque de cette adresse
//        AdressesRefusees.add(rec);
//        AdressesRefusees.add(masque);
//        //On n'oublie pas de lire la ligne suivante
//        rec = r.readLine();
//    }
//
//
//    //On attend la connexion du client et on crée le clientSocket
//    Socket clientSocket = serverSocket.accept();
//
//    //Ici, on va récupérer l'adresse IP local
//    InetAddress inetAddressLocalhost = InetAddress.getLocalHost();
//    String adresseIP = inetAddressLocalhost.getHostAddress();
//
//            System.out.println("Nouvelle connexion entrante : " + clientSocket.getInetAddress());
//
//>>>>>>> 7e441537e242ee6e154d0d6aa4b24ae1e43fbd54
//}
