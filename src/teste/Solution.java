package teste;
import java.io.*;
import java.math.*;
import java.security.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class Solution {
    public static void main(String[] args) throws IOException {
        List<Integer> processorsTimeList = new ArrayList<Integer>();
        processorsTimeList.add(10);
        processorsTimeList.add(20);
        //processorsTimeList.add(5);
        
        List<Integer> tasksTimeList = new ArrayList<Integer>();
        tasksTimeList.add(2);
        tasksTimeList.add(3);
        tasksTimeList.add(1);
        tasksTimeList.add(2);
        tasksTimeList.add(5);
        tasksTimeList.add(1);
        tasksTimeList.add(4);
        tasksTimeList.add(3);
        //tasksTimeList.add(3);
        //tasksTimeList.add(3);
        
        // Calula o tamanho m�ximo de cada grupo de combina��es das tasks
        Integer groupSize = tasksTimeList.size() / processorsTimeList.size();
        
        // Calcula as combina��es de tarefas
        List<List<Integer>> tasksCombinations = new ArrayList();
        try {
        	getCombinations(tasksCombinations, new ArrayList<Integer>(), groupSize, tasksTimeList.size(), 0, 0, 0);
        } catch (StackOverflowError exception) {
        	System.out.println("Erro de limite de mem�ria: verifique a possibilidade de aumentar a mem�ria da m�quina");
        }
        
        // Calcula as combina��es de grupos de tarefas, com base na quantidade de processadores dispon�veis
        List<List<Integer>> combinationsCore = new ArrayList();
        try {	        
	        getCombinations(
	        		combinationsCore, 
	        		new ArrayList<Integer>(), 
	        		processorsTimeList.size(), 
	        		tasksCombinations.size()
	        		, 0, 0, 0
	        );
        } catch (StackOverflowError exception) {
        	System.out.println("Erro de limite de mem�ria: verifique a possibilidade de aumentar a mem�ria da m�quina");
        }
        
        // Limpa a lista de combina��es poss�veis, removenos as combinacoes com a MESMA TAREFAS em grupos diferentes
        combinationsCore = clearDirtyCombinations(combinationsCore, tasksCombinations);
        
        int minimunTime = -1;
        for (List<Integer> combCore : combinationsCore) {
        	System.out.println(combCore);
        	
        	// v�riavel que serve de referencia para pegar o tempo do respectivo processador desse grupo 
        	int idProcessorTime = -1;
        	
        	// v�riavel que registra o tempo m�ximo do grupo dentro dessa combina��o de cores
        	int maxTimeGroup = 0;
        	
        	for(Integer idCC: combCore) {
        		
        		int processorTime = processorsTimeList.get(++idProcessorTime);
        		List<Integer> tasksIDs = tasksCombinations.get(idCC);
        		List<Integer> tasksTime = new ArrayList<Integer>();
            	
        		for(Integer p: tasksIDs) {
            		tasksTime.add(tasksTimeList.get(p));
            	}
        		
        		// calcula o tempo m�ximo de execu��o desse grupo de tarefas
        		int maxTimeSubGroup = calculateMaxTime(processorTime, tasksTime);
        		
        		// faz a verifica��o para saber se � maior que o tempo m�ximo j� registrado
        		if(maxTimeSubGroup > maxTimeGroup)
        			maxTimeGroup = maxTimeSubGroup;
        		
        		System.out.println("- t: " + processorTime + ";  tasksIDs: " + tasksIDs + " tasksTimes: " + tasksTime + "; maxTime: " + maxTimeGroup);
        	}
        	
        	if(minimunTime > maxTimeGroup || minimunTime == -1)
        		minimunTime = maxTimeGroup;
        }
        
        // imprime o resultado
        System.out.println("Tempo m�nimo: " + minimunTime);
		    
       // return minimunTime;
    }  
   
    /**
     * M�todo respons�vel por limpar a lista de combina��es poss�veis, removendo as combinacoes onde a  MESMA TAREFA 
     * est� em grupos diferentes. CADA TAREFA ser� executado em um �nico core
     * @param combinationsCore
     * @param combinations
     * @return
     */
	private static List<List<Integer>> clearDirtyCombinations(List<List<Integer>> combinationsCore, List<List<Integer>> combinations) {
		List<List<Integer>> cleanedList = new ArrayList();
		
		for (List<Integer> combCore : combinationsCore) {
        	//System.out.println(combCore);
        	List<Integer> taskIDLoaded = new ArrayList<Integer>();
        	boolean combOk = true;
        	for(Integer idCC: combCore) {
        		List<Integer> tasksIds = combinations.get(idCC);
        		
        		//System.out.println("-- " + tasksIds);
        		for(Integer taskId: tasksIds) {
        			//System.out.println(tasksIds + " contains " + taskId + " = " + taskIDLoaded.contains(taskId)) ;
        			if(taskIDLoaded.contains(taskId)) {
        				//System.out.println("*** Removing!");
        				combOk = false;
        				break;
        			}
        			
        			taskIDLoaded.add(taskId);        				
        		}
        	}

    		
    		if(combOk) {
    			cleanedList.add(combCore);
    			// System.out.println(" add " + combCore);
    		}
		}
		
		return cleanedList;
		
	}

	/**
	 * M�todo respons�vel por calcular o tempo m�ximo que a tarefa pode levar
	 * @param tempo
	 * @param taskTimeList
	 * @return
	 */
	private static int calculateMaxTime(Integer tempo, List<Integer> taskTimeList) {
		Integer maxTime = 0;
		for(Integer l: taskTimeList) {
			Integer totalTime = l + tempo; 
			if(totalTime > maxTime)
				maxTime = totalTime;
		}
		return maxTime;
	}


	/**
	 * M�todo respons�vel montar as combina��es possiveis, conforme o tamanho da lista de elementos
	 * @param combinations
	 * @param subGroup
	 * @param groupSize
	 * @param listSize
	 * @param referencePos
	 * @param currentPos
	 * @param lap
	 */
	private static void getCombinations(List<List<Integer>> combinations, List<Integer> subGroup, Integer groupSize, int listSize, int referencePos, int currentPos, int lap) {
		if(referencePos != listSize-groupSize+1){
			
			// Se � o in�cio do grupo, adiciona a posi��o de referencia
			if(subGroup.isEmpty())
				subGroup.add(referencePos);
			
			// calcula o proximo elemento que deve entrar na combina��o
			int pos = currentPos + lap;

			// como n�o pode ter tarefas repeditas no mesmo grupo, caso aconte�a, adicionar 1 para compensar e evitar a repeti��o de elementos
			if(pos >= referencePos)
				pos ++;
			
			// verifica��o para n�o adicionar posi��es inv�lidas
			if(pos < listSize)
				subGroup.add(pos);
			
			// verifica se j� fechou o grupo, ou seja, se atingiu a qtde limite de elementos
			if(subGroup.size() == groupSize || pos >= listSize) {
				// SE SIM, faz as tratativas para iniciar a nova combina��o
				
				currentPos ++;
				if(currentPos == listSize - 1) {
					// se a posi��o corrente atingiu o limite, incrementa a posi��o de referencia e zera a posi��o corrente
					referencePos ++;
					currentPos = 0;
				}
				
				lap = 0;
				if(subGroup.size() == groupSize) {
					// Adiciona a combina��o na lista
					combinations.add(subGroup);
					//System.out.println(subGroup);
				}
				
				// Reinicia o grupo de combina��o
				subGroup = new ArrayList<Integer>();
				
			} else {
				lap ++;
				//currentPos ++;
			}
			getCombinations(combinations, subGroup, groupSize, listSize, referencePos, currentPos, lap);	
		}
			
	}
	    
}
