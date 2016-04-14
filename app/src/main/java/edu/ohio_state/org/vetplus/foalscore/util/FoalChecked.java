package edu.ohio_state.org.vetplus.foalscore.util;

/**
 * Created by User Name on 4/8/2015.
 */
public class FoalChecked {
    private String id;
    private String name;
    private String age;
    private String gender;
    private String breed;
    private String temperature;
    private String dystocia;
    private String respiratoryRate;
    private String heartRate;
    private String dateCreated;
    private String isChecked;

    public FoalChecked (String id, String name, String age, String gender, String breed, String dateCreated, String temperature, String respiratoryRate,String heartRate, String dystocia, String isChecked) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.breed = breed;
        this.dateCreated = dateCreated;
        this.temperature = temperature;
        this.respiratoryRate = respiratoryRate;
        this.heartRate = heartRate;
        this.dystocia = dystocia;
        this.isChecked = isChecked;
    }

    public String getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(String isChecked) {
        this.isChecked = isChecked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getDystocia() {
        return dystocia;
    }

    public void setDystocia(String dystocia) {
        this.dystocia = dystocia;
    }

    public String getRespiratoryRate() {
        return respiratoryRate;
    }

    public void setRespiratoryRate(String respiratoryRate) {
        this.respiratoryRate = respiratoryRate;
    }

    public String getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(String heartRate) {
        this.heartRate = heartRate;
    }
}
