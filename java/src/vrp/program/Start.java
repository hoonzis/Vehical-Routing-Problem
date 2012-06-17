package vrp.program;

import java.io.IOException;

public class Start {

	public static void main(String[] args) throws IOException{
		
		if(args.length!=2){
			System.out.println("Two parameters are obligatory. Filename and  'sweep' or 'clark' to choose the algorithm");
			System.out.println("USAGE: Start [filename] [sweep|clark]");
		}
		
		String fileName = args[0];
		
		VRPProgram.loadData(fileName);
		Stopwatch s = new Stopwatch();
		s.start();
		
		if(args[1].equals("sweep")){
			String output = VRPProgram.sweep();
			System.err.println(output);
		}else{
			VRPProgram.clarkWright();
		}
		s.stop();
		System.out.println(s.toString());
		//MyUtils.generateRandomNodes(100,300,15);

	}
}
