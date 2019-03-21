package br.sc.senai.envd.servidor.cripto;

/**
 * Classe de trabalho que implementa os métodos criptográficos necessários para
 * o funcionamento do processo de criptografia entre um Cliente e o Servidor.
 * Esta classe atende somente ao servidor de comunicação. * 
 * 
 */
import br.sc.senai.envd.cliente.cripto.Cripto_Cliente;
import java.io.InputStream; 
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
KeyStore de criptografia gerado com prazo de vida de 10 anos.
Utilizado apenas para armazenar as chaves Pública e Privada,
necessárias ao processo de criptografia. Utiliza algorítmo RSA.
Comando para geração do certificado:
keytool -genkey -alias autentica -keyalg RSA -keypass seg2012 -storepass seg2012 -keystore env_digital.jks -validity 3650
*/
public class Cripto_Server {
    
    private static final String KEY_STORE_PATH = "/conf/envdigital.jks";
    private static final String alias = "envdigital"; 
    private static String password = null;
    private final InputStream inputStreamKeyStore = getClass().getResourceAsStream(KEY_STORE_PATH);
       
    public Cripto_Server(){
    }

    /**
     * Método que decripta dados, encriptados pela chave Pública
     contida no Keystore.Após decriptado, retorna um array de bytes dos dados.
     * @param byteTextoCriptografado , chavePriv
     * @param chavePrivada
     * @return
     */
    public static byte[] decriptarComChavePrivada (byte[] byteTextoCriptografado, PrivateKey chavePrivada){
        try {
            Cipher cifra = Cipher.getInstance(Cripto_Cliente.ALGORITMO_ASSIMETRICO);
            cifra.init(Cipher.DECRYPT_MODE, chavePrivada);
            System.out.println("Crypto_Server: Tam bytes decriptados: " + byteTextoCriptografado.length);
            return cifra.doFinal(byteTextoCriptografado);
        }
        catch ( NoSuchAlgorithmException e ) {
            System.out.println("Classe Crypto_Server - Erro no Algoritmo RSA na decriptação..." + e.getMessage());
        }  catch (NoSuchPaddingException e) {
            System.out.println("Classe Crypto_Server - Erro no Padding do RSA na decriptação..." + e.getMessage());
        } catch (InvalidKeyException e) {
            System.out.println("Classe Crypto_Server - Erro na Chave RSA na  decriptação..." + e.getMessage());
        } catch (IllegalBlockSizeException e) {
            System.out.println("Classe Crypto_Server - Erro no tamanho de Bloco RSA na decriptação..." + e.getMessage());
        } catch (BadPaddingException e) {
            System.out.println("Classe Crypto_Server - Erro Padding inválido no RSA para decriptação..." + e.getMessage());
        }
        return null;
    }
    
    /**
     * Método para ler a chave Privada do Keystore
     * Retorna um objeto chave Privada PrivateKey
     * @return
     * @throws Exception 
    */
    public PrivateKey getPrivateKeyFromFile() throws Exception {
        KeyStore keyStore = KeyStore.getInstance ( "JKS" );
        keyStore.load( inputStreamKeyStore, password.toCharArray());
        inputStreamKeyStore.close();
        Key key = keyStore.getKey( alias, password.toCharArray() );
        if( key instanceof PrivateKey ) {
            return (PrivateKey) key;
        }
        return null;
    }
    /**
     Método para ler a chave Pública do Keystore
     Retorna um objeto chave Pública PublicKey
    * @return
    * @throws Exception 
    */
    
    public PublicKey getPublicKeyFromFile() throws Exception {
        KeyStore keyStore = KeyStore.getInstance ( "JKS" );
        keyStore.load( inputStreamKeyStore, password.toCharArray() );
        Certificate certificado = keyStore.getCertificate( alias );
        PublicKey chavePublica = certificado.getPublicKey();
        System.out.println("Chave Pública: " + chavePublica.toString());
        return chavePublica;
    }

    /**
     * Método para encriptar dados com a chave Simétrica.
     * @param bytesTextoPuro
     * @param bytesChaveSimetrica
     * @return bytes encriptados
     */
    public static byte[] encriptarComChaveSimetrica(byte[] bytesTextoPuro, byte[] bytesChaveSimetrica){
         try {
             Cipher cifra = Cipher.getInstance(Cripto_Cliente.ALGORITMO_SIMETRICO);
             cifra.init(Cipher.ENCRYPT_MODE, new SecretKeySpec (bytesChaveSimetrica,Cripto_Cliente.CHAVE_ALGORITMO_SIMETRICO));
             return cifra.update(bytesTextoPuro);
         } catch (NoSuchAlgorithmException ex){
            System.out.println("Erro no Algoritmo de  decriptação simétrica, Verifique! " + ex.getMessage());
         } catch (NoSuchPaddingException ex) {
            System.out.println("Erro no Padding da decriptação simétrica, Verifique! " + ex.getMessage());
         } catch (InvalidKeyException ex) {
            System.out.println("Erro na Chave simétrica, Verifique! " + ex.getMessage());
         }
        return null;
     }
    /**
     * Método para decriptar dados com uma chave Simétrica.
     * @param bytesTextoCriptografado
     * @param bytesChaveSimetrica
     * @return bytes decriptados.
     */
    public static byte[] decriptarComChaveSimetrica(byte[] bytesTextoCriptografado, byte[] bytesChaveSimetrica){
         try {
             Cipher cifra = Cipher.getInstance(Cripto_Cliente.ALGORITMO_SIMETRICO);
             SecretKeySpec chaveSimetrica = new SecretKeySpec (bytesChaveSimetrica,Cripto_Cliente.CHAVE_ALGORITMO_SIMETRICO);
             cifra.init(Cipher.DECRYPT_MODE, chaveSimetrica);
             return cifra.update(bytesTextoCriptografado);             
         }
         catch (NoSuchAlgorithmException ex){
           System.out.println("Erro no Algoritmo de  decriptação simétrica, Verifique! " + ex.getMessage());
         } catch (NoSuchPaddingException ex) {
            System.out.println("Erro no Padding da decriptação simétrica, Verifique! " + ex.getMessage());
         } catch (InvalidKeyException ex) {
            System.out.println("Erro na Chave simétrica, Verifique! " + ex.getMessage());
         }
        return null;
     }
    
    public static boolean isSenhaKeyStoreNaoInformada(){
        return password == null || password.trim().equals("");
    }

    public static String getPassword() {
        return password;
    }
    
    public static void setPassword(String password) {
        Cripto_Server.password = password;
    }
}