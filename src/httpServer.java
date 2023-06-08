package src;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;


public class httpServer {
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        //Récupération du fichier webconf
        File fichierXML = new File("webconf.xml");
        //Chargement du fichier XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(fichierXML);

        //Création des variables nécessaires au bon chargement du fichier
        int portNumber = 80; // Port par défaut
        String repertoire = "";
        ArrayList<String> AdressesRefusees = new ArrayList<>();
        ArrayList<String> AdressesAcceptees = new ArrayList<>();
        String cheminLogAccess = "";
        String cheminLogError = "";

        //On peut maintenant commencer à extraire les informations du serveur
        Element fichierElements = document.getDocumentElement();
        NodeList list = fichierElements.getChildNodes();


        for (int i = 0; i < list.getLength(); i++) {

            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE){
                Element element = (Element) node;
                //On récupère le nom de la balise et selon le cas on modifie une valeur
                switch (element.getNodeName()){
                    case "port":
                        portNumber = Integer.parseInt(element.getTextContent());
                        if ((portNumber < 0) || (portNumber > 65535)){
                            portNumber = 80;
                        }
                        break;
                    case "root":
                        repertoire = element.getTextContent();
                        break;
                    case "accept":
                        AdressesAcceptees.add(element.getTextContent());
                        AdressesAcceptees.add(calculerMasque(element.getTextContent()));
                        break;
                    case "reject":
                        AdressesRefusees.add(element.getTextContent());
                        AdressesRefusees.add(calculerMasque(element.getTextContent()));
                        break;
                    case "accesslog":
                        cheminLogAccess = repertoire + element.getTextContent();
                        break;
                    case "errorlog":
                        cheminLogError = repertoire + element.getTextContent();
                        break;

                }
            }
        }



        // Création du serveur avec le numéro de port
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Serveur Web en attente de connexions sur le port " + portNumber + "...");

            //On attend la connexion du client et on crée le clientSocket
            Socket clientSocket = serverSocket.accept();

            //Ici, on va récupérer l'adresse IP local
            InetAddress inetAddressLocalhost = InetAddress.getLocalHost();
            String adresseIP = inetAddressLocalhost.getHostAddress();

            System.out.println("Nouvelle connexion entrante : " + clientSocket.getInetAddress());



            //Ici, on crée un boolean permettant de choisir un seul des cas suivants : Adresse autorisée, Adresse refusée, Adresse inconnue
            boolean IPConnu = false;

            //On parcourt la liste d'adresses acceptées
            for (int i = 0; i < AdressesAcceptees.size() / 2; i++) {
                //On regarde si l'adresse réseau correspond à l'adresse réseau calculée par la méthode calculerAdresseReseau
                if (AdressesAcceptees.get(i * 2).equals(calculerAdresseReseau(adresseIP, AdressesAcceptees.get(i * 2 + 1)))) {
                    System.out.println("Connection réalisée");
                    System.out.println("\nAdresse IP : " + adresseIP);
                    System.out.println("Masque réseau : " + AdressesAcceptees.get(i * 2 + 1));
                    System.out.println("Adresse réseau : " + AdressesAcceptees.get(i * 2) + "\n");
                    //On boucle ensuite à l'infini pour récupérer les requêtes du serveur
                    while (true) {
                        BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        //On crée un writer envoyant les choses demandées par le serveur
                        OutputStream versClient = clientSocket.getOutputStream();
                        DataOutputStream versClientData = new DataOutputStream(versClient);

                        //on veut vérifier que le fichier root existe donc on va vérifier que fileName est un dossier
                        File root = new File(repertoire);
                        if (!root.isDirectory()) {
                            erreurChemin(versClient, adresseIP);
                            System.out.println("Le fichier root n'existe pas");
                        }

                        FileWriter accessWriter = new FileWriter(cheminLogAccess, true);
                        //On crée un lecteur permettant de récupérer les requêtes du secteur



                        //On récupère tout d'abord la requête
                        String request = fromClient.readLine();
                        System.out.println("Requête du client : " + request + "\n");

                        String[] demande = new String[0];
                        if (request != null) {
                            String date = String.valueOf(LocalDateTime.now());
                            String[] temps = date.split("T");
                            accessWriter.write("Date : " + temps[0] + " - Heure : " + temps[1].substring(0, 8) + " - Adresse IP : " + adresseIP + " - Requête : " + request + "\n");
                            request = request.replace("GET /", "");
                            demande = request.split(" ");
                        }

                        File file = null;


                        //On crée le fichier duquel on va lire puis envoyer les informations au serveur
                        if (demande.length != 0){
                            if (demande[0].equals("")){
                                file = new File(repertoire + "index.html");
                            }
                            else if (demande[0].equals("serveur/status")){
                                afficherEtatMachine(versClientData);
                            }
                            else {
                                file = new File(repertoire + demande[0]);
                            }
                        }


                        //On vérifie si le fichier existe et si c'est bien un fichier (et pas un répertoire par exemple)
                        if (file != null) {
                            //On crée un nouveau lecteur pour le fichier
                            FileInputStream fichier = new FileInputStream(file);
                            if (file.exists() && file.isFile()) {
                                byte[] buffer = new byte[(int) file.length()];
                                fichier.read(buffer);
                                fichier.close();




                                // Écrire les en-têtes de la réponse
                                versClientData.writeBytes("HTTP/1.1 200 OK\r\n");
                                versClientData.writeBytes("Content-Encoding: gzip\r\n");
                                versClientData.writeBytes("Content-Length: " + buffer.length + "\r\n");
                                versClientData.writeBytes("\r\n");

                                // Créer le flux de sortie gzip
                                GZIPOutputStream gzip = new GZIPOutputStream(versClientData);


                                // Compresser et écrire le contenu du fichier dans le flux gzip
                                gzip.write(buffer);
                                gzip.close();

                                versClientData.flush();

                            } else {
                                // Le fichier demandé n'existe pas ou n'est pas un fichier valide
                                erreurChemin(versClient, adresseIP);
                            }
                        }
                        accessWriter.flush();
                        versClient.close();
                        fromClient.close();
                        //On attend la connexion du client et on crée le clientSocket
                        clientSocket = serverSocket.accept();
                        System.out.println("Nouvelle connexion entrante : " + clientSocket.getInetAddress());
                    }
                }
            }
            String typeErreur = "";
            //Adresse non présente dans la liste des acceptées, on regarde donc si elle est présente dans la liste des refusées
            for (int i = 0; i < AdressesRefusees.size() / 2; i++) {
                if (AdressesRefusees.get(i * 2).equals(calculerAdresseReseau(adresseIP, AdressesRefusees.get(i * 2 + 1)))) {
                    IPConnu = true;
                    typeErreur = "adresse IP refusée.";
                    System.out.println("Erreur lors de l'exécution du serveur : " + typeErreur);
                    erreurIP(clientSocket.getOutputStream(), adresseIP, typeErreur);
                    serverSocket.close();
                }
            }
            //Ici, si IPConnu est resté à false, c'est qu'on n'est pas rentré dans les deux cas précédents ce qui signifie que l'adresse IP est inconnue
            if (!IPConnu) {
                typeErreur = "adresse IP inconnue.";
                System.out.println("Erreur lors de l'exécution du serveur : "+ typeErreur);
                erreurIP(clientSocket.getOutputStream(), adresseIP, typeErreur);
            }

        } catch (IOException e) {
            System.out.println("Erreur lors de l'exécution du serveur : " + e.getMessage());
        }
    }


    public static String calculerMasque(String networkAddress) {
        try {
            int subnetIndex = networkAddress.indexOf('/');
            int subnetMask = Integer.parseInt(networkAddress.substring(subnetIndex + 1));

            int subnet = 0xFFFFFFFF << (32 - subnetMask);

            byte[] subnetMaskBytes = new byte[4];
            subnetMaskBytes[0] = (byte) ((subnet >> 24) & 0xFF);
            subnetMaskBytes[1] = (byte) ((subnet >> 16) & 0xFF);
            subnetMaskBytes[2] = (byte) ((subnet >> 8) & 0xFF);
            subnetMaskBytes[3] = (byte) (subnet & 0xFF);

            InetAddress subnetMaskInet = InetAddress.getByAddress(subnetMaskBytes);
            return subnetMaskInet.getHostAddress();
        } catch (UnknownHostException | StringIndexOutOfBoundsException | NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static String calculerAdresseReseau(String ipAddress, String subnetMask) {
        try {
            int subnetMaskBits = 0;
            String[] subnetMaskOctets = subnetMask.split("\\.");
            for (String octet : subnetMaskOctets) {
                int value = Integer.parseInt(octet);
                subnetMaskBits += Integer.bitCount(value);
            }

            byte[] ipBytes = InetAddress.getByName(ipAddress).getAddress();
            int ipInt = 0;
            for (byte b : ipBytes) {
                ipInt = (ipInt << 8) + (b & 0xFF);
            }
            int networkInt = ipInt & (0xFFFFFFFF << (32 - subnetMaskBits));

            byte[] networkAddressBytes = new byte[4];
            for (int i = 3; i >= 0; i--) {
                networkAddressBytes[i] = (byte) (networkInt & 0xFF);
                networkInt >>>= 8;
            }

            InetAddress networkAddressInet = InetAddress.getByAddress(networkAddressBytes);
            return networkAddressInet.getHostAddress() + "/" + subnetMaskBits;
        } catch (UnknownHostException | NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }



    private static void erreurChemin(OutputStream os, String adresseIP) throws IOException
    {
        os.write("HTTP/1.1 404 Erreur, Dossier introuvable".getBytes());

        String message = "Erreur 404 - Dossier introuvable";

        String pageHTML = "<!DOCTYPE html>\n<html lang=\"fr\">\n\n<head>\n\t" +
                "<meta http-equiv=Content-Type content=\"text/html; charset=utf-8\">\n" +
                "\t<title>Erreur</title>\n</head>\n" +
                "<body>\n" +
                "        <section>\n" +
                "        <h1>" + message + "</h1>\n" +
                "        </section>\n" +
                "    </main>\n" +
                "</body>\n" +
                "</html>";

        String response = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Length: " + pageHTML.length() + "\r\n" +
                "Content-Type: text/html\r\n" +
                "\r\n" +
                pageHTML;

        os.write(response.getBytes());
        String erreur = "Erreur 404, erreur dossier introuvable";
        ecrireFichierErrorlog(adresseIP, erreur);
    }




    private static void erreurIP(OutputStream os, String adresseIP, String typeErreur) throws IOException
    {
        String message = "Erreur 403 - Erreur IP : " + typeErreur;

        String pageHTML = "<!DOCTYPE html>\n<html lang=\"fr\">\n\n<head>\n\t" +
                "<meta http-equiv=Content-Type content=\"text/html; charset=utf-8\">\n" +
                "\t<title>Erreur</title>\n</head>\n" +
                "<body>\n" +
                "        <section>\n" +
                "        <h1>" + message + "</h1>\n" +
                "        </section>\n" +
                "    </main>\n" +
                "</body>\n" +
                "</html>";

        String response = "HTTP/1.1 403 Forbidden\r\n" +
                "Content-Length: " + pageHTML.length() + "\r\n" +
                "Content-Type: text/html\r\n" +
                "\r\n" +
                pageHTML;

        os.write(response.getBytes());
        os.flush();
        String erreur = "Erreur 403, Erreur IP : " + typeErreur;
        ecrireFichierErrorlog(adresseIP, erreur);
    }

    private static void ecrireFichierErrorlog(String adresseIP, String erreur) throws IOException {
        System.out.println(erreur);
        FileWriter errorWriter = new FileWriter("FichiersSiteWeb/errorlog.txt", true);
        String date = String.valueOf(LocalDateTime.now());
        String[] temps = date.split("T");
        errorWriter.write("Date : " + temps[0] + " - Heure : " + temps[1].substring(0, 8) + " - Adresse IP : " + adresseIP + " - Erreur : " + erreur + "\n");
        errorWriter.flush();
    }

    private static void afficherEtatMachine(OutputStream versClient) throws IOException {
        // Récupérer les informations de l'état de la machine
        String etatMachine = obtenirEtatMachine();

        // Générer la page HTML d'affichage de l'état de la machine
        String pageHTML = "<html>\n\t<head>\n\t\t<title>Etat de la machine</title>\n\t</head>\n\t<body>\n\t\t<h1>Etat de la machine</h1>\n\t\t<p>" + etatMachine;
        pageHTML += "</p>\n\t</body>\n</html>";

        // Envoyer la réponse HTTP au client
        String httpResponse = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nContent-Length: " + pageHTML.length() + "\r\n\r\n" + pageHTML;

        versClient.write(httpResponse.getBytes());
    }

    private static String obtenirEtatMachine() {
        double nombreProcessus = Runtime.getRuntime().availableProcessors();
        long utilisationMemoire = obtenirMemoireUtilisee();
        long memoireLibre = obtenirMemoireRestante();

        String etatMachine = "Nombre de processus : " + nombreProcessus + "<br>Utilisation de la memoire : " + utilisationMemoire + " Mo<br>Memoire libre : " + memoireLibre + " Go";

        return etatMachine;
    }

    private static long obtenirMemoireUtilisee() {
        long memoireUtilise = Runtime.getRuntime().freeMemory();
        memoireUtilise = memoireUtilise / (1024 * 1024); // Convertir en Mo
        return memoireUtilise;
    }

    private static long obtenirMemoireRestante() {
        long memoireLibre = new File("/").getFreeSpace();
        memoireLibre = memoireLibre / (1024 * 1024 * 1024); // Convertir en Mo
        return memoireLibre;
    }


}