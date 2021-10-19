package bd_tp6;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;




public class Tp6 {
	// Connexion directe au serveur Neo4J
	private static Driver driver = GraphDatabase.driver("bolt://192.168.56.50");
	
	public static void afficherMenu() {
		// Afficher le menu
		System.out.println("////////////////////////////////////////////////////");
		System.out.println("1. Lister les films disponibles");
		System.out.println("2. Lister les personnes disponibles");
		System.out.println("3. Afficher les 3 films les mieux notés");
		System.out.println("4. Afficher au plus 5 films 'proches'");
		System.out.println("0. Quitter");
		System.out.println("////////////////////////////////////////////////////");
	}
	
	public static void listerFilmsDispos() {
		// Démarrer une session
		Session session = driver.session();
		
		// Traitement
		StatementResult result = session.run( 
				"match (m : Movie) " + 
				"return distinct m " + 
				"order by m.released DESC" );
		
		// Affichage
		while ( result.hasNext() )
		{
			Record record = result.next();
			Node n = record.get( "m" ).asNode();
			String title = n.get("title").asString();
			String tagline = n.get("tagline").asString();
			int released = n.get("released").asInt();
			System.out.println("	"+ title + " - " + released + " (" + tagline + ")");
		}
		
		// Fermeture de la session
		session.close();
		
	}
	
	public static void listerPersonnesDispos() {
		// Démarrer une session
		Session session = driver.session();
		
		// Traitement
		StatementResult result = session.run( 
				"match (Person)-[]->(m :Movie) " + 
				"with distinct m.title as titles " + 
				"order by titles ASC " + 
				"match (p :Person)-[r]->(m1 :Movie) " + 
				"where m1.title = titles " + 
				"with p.name as n, p.born as b, type (r) as ty, collect(m1.title) as t " + 
				"order by ty ASC " + 
				"return n,b,collect({typ:ty,titl:t}) as c " + 
				"order by n ASC" );
		
		/*StatementResult result = session.run( 
				"match (Person)-[]->(m :Movie) " + 
				"with distinct m.title as titles " + 
				"order by titles ASC " + 
				"match (p :Person)-[r]->(m1 :Movie) " + 
				"where m1.title = titles " + 
				"with p.name as n, p.born as b, type (r) as ty, collect(m1.title) as t " + 
				"order by ty ASC " + 
				"return n,b,collect(ty+' : '+t) as c " + 
				"order by n ASC" );*/
		
		
		// Affichage
		while ( result.hasNext() )
		{
			Record record = result.next();
			String name = record.get("n").asString();
			String born = "?";
			if(String.valueOf(record.get("b")) != "NULL") {
				born = String.valueOf(record.get("b"));
			}
			
			List<Object> collect = record.get("c").asList();
			System.out.println( name + " (" + born + ")");
			for(int i=0; i<collect.size();i++) {
				Map<Object,Object> c = (Map<Object, Object>) collect.get(i);
				for(Object d : c.keySet()) {
					if(d.equals("typ")) {
						System.out.print("	" + c.get(d) + " ");
					}
					else {
						System.out.println(c.get(d));
					}
				}
				//System.out.println("	" + collect.get(i).toString());
			}
			/*for(Object d : ((Map<Object, Object>) collect).keySet()) {
				System.out.println(d);
			}*/
		}
		
		// Fermeture de la session
		session.close();
	}

	public static void afficherFilmsMieuxNotes() {
		// Démarrer une session
		Session session = driver.session();
				
		// Traitement
		StatementResult result = session.run( 
				"match(m:Movie)-[r:REVIEWED]-(p) " + 
				"return distinct m.title, r.rating " + 
				"order by r.rating DESC " + 
				"Limit 3" );
				
		// Affichage
		while ( result.hasNext() )
			{
				Record record = result.next();
				String title = record.get("m.title").asString();
				int rating = record.get("r.rating").asInt();
				System.out.println("	"+ title + " - " + rating);
			}
				
		// Fermeture de la session
		session.close();
	}
	
	public static void afficherFilmsProches(String film) {
		// Démarrer une session
		Session session = driver.session();
						
		// Traitement
		/*StatementResult result = session.run( 
			"match (p: Person)-[a:ACTED_IN]->(m: Movie) " + 
			"where m.title = '" + film + "' " + 
			"with p as name " + 
			"match (p2: Person)-[a2:ACTED_IN]->(m2: Movie) " + 
			"where m2.title <> '" + film + "' " + 
			"AND p2 = name " + 
			"return m2.title, count(p2) as nb " + 
			"order by nb DESC, m2.title ASC" );*/
		
		StatementResult result = session.run( 
				"match (m2: Movie)<-[a2: ACTED_IN]-(p: Person)-[a: ACTED_IN]->(m: Movie) " + 
				"where m.title = '" + film + "' " +
				"return m2.title, count(p) as nb " + 
				"order by nb DESC, m2.title ASC " + 
				"Limit 5" );
						
		// Affichage
		while ( result.hasNext() )
			{
				Record record = result.next();
				String title = record.get("m2.title").asString();
				int nb = record.get("nb").asInt();
				System.out.println("	"+ title + " - " + nb);
			}
				
		// Fermeture de la session
		session.close();
	}
	
	public static void main(String [] a){
	
	// Démarrer une session
	Session session = driver.session();
	
	Scanner in = new Scanner(System.in);
	Scanner in2 = new Scanner(System.in);

	// Traitement des demandes
	boolean quitter = false;

	int menuItem;

	do {
		afficherMenu();
		System.out.print("Veuillez choisir un élément du menu : ");
		menuItem = in.nextInt();
		switch (menuItem) {
		case 1:
			System.out.println("Films disponibles : ");
			listerFilmsDispos();
			break;
		case 2:
			System.out.println("Personnes disponibles : ");
			listerPersonnesDispos();
			break;
		case 3:
			System.out.println("3 films les mieux notés : ");
			afficherFilmsMieuxNotes();
			break;
		case 4:
			System.out.println("Donner le titre du film : ");
			String film = in2.nextLine();
			afficherFilmsProches(film);
			break;
		case 0:
			quitter = true;
			break;
		default:
			System.out.println("Choix invalide.");
		}
	} while (!quitter);
	
	
	// Fermeture de la connexion
	driver.close();
	}
}

