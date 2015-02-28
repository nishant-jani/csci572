import java.io.*;
import java.util.*;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.*;

class documentDuplication
{
	public static void main(String args[]) throws UnsupportedEncodingException
	{
		boolean doc1_hash[] = hash("hello how are you ? <HTML> <HEAD>");
		boolean doc2_hash[] = hash("hello how are you ?");
		double similar = 0;
		double diff = 0;
		for(int i=0; i<128; i++)
		{
			//System.out.println(doc1_hash[i] + " " + doc2_hash[i]);
			if(doc1_hash[i]^doc2_hash[i]==false)
			similar++;
			else
			diff++;	
		}
		System.out.println("Similarity: " + similar/128);

	}

	public static boolean[] hash(String str) throws UnsupportedEncodingException
	{
		int token_fingerprint[] = new int[128];
		StringTokenizer defaultTokenizer = new StringTokenizer(str);
		while(defaultTokenizer.hasMoreTokens()) 
		{
			String token = defaultTokenizer.nextToken();
			byte[] byte_token = token.getBytes("UTF-8");
			try	
			{	
				MessageDigest md = MessageDigest.getInstance("MD5");
				byte[] token_hash = md.digest(byte_token);
				//System.out.println(token_hash);
				//BigInteger b = new BigInteger(1, token_hash);
				//String md5_hash = b.toString();
				//System.out.println(md5_hash);
				int i = 0;
				for (int j = 0; j < token_hash.length; j++)
				 {
					for (int k = 0; k < 8; k++)
					 {
						if ((token_hash[j] >> (7 - k) & 0x01) == 1)
							token_fingerprint[j * 8 + k] += 1;
						else
							token_fingerprint[j * 8 + k] -= 1;
					}
				 }
				 //end of digit fingerprint creation per token		
			}	

			catch(NoSuchAlgorithmException e)
			{
				e.printStackTrace();
			}

			

			

		} // end while
		//System.out.println(token_fingerprint.length);
		boolean fingerprint[] = new boolean[128];
		for (int i=0; i<128; i++) {
			if (token_fingerprint[i] >= 0) {
				fingerprint[i]=true;
			} else {
				fingerprint[i]=false;
				}
			}
		return fingerprint;
		
	}


	
}