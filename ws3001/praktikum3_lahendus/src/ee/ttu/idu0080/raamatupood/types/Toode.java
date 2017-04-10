package ee.ttu.idu0080.raamatupood.types;
import java.math.BigDecimal;
//serialiseerib klassi state'i (olukorra)
import java.io.Serializable;

public class Toode implements Serializable {
	private int kood;
	private String nimetus;
	private BigDecimal hind;
	
	public Toode(int kood, String nimetus, BigDecimal hind){
		this.kood = kood;
		this.nimetus = nimetus;
		this.hind = hind;
	}
	
	public int getKood(){
		return kood;
	}
	
	public void setKood(int kood){
		this.kood = kood;
	}
	
	public String getNimetus(){
		return nimetus;
	}
	
	public void setNimetus(String nimetus){
		this.nimetus = nimetus;
	}
	
	public BigDecimal getHind(){
		return hind;
	}
	
	public void setHind(BigDecimal hind){
		this.hind = hind;
	}
}