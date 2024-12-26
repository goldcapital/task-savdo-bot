package org.programming.tasksavdobot.dto;

import lombok.Data;
import org.programming.tasksavdobot.enums.Step;
@Data
public class ProfileDTO {
    private Long id;
    private String fullName;
    private String productName;
    private  Long categoryId;
    private  String  walking;
    private Step step=Step.LANGUAGE_UZ;
    private String type;
    private Long chatId;
    private String phone;
    private String currentStep;
    private String additionalInfo;
    private String productType;
    private String imagePath;
    private Double price;


    public void reset() {
        this.currentStep=null;
        this.categoryId = null;
        this.imagePath = null;
        this.productName = null;
        this.additionalInfo=null;
        this.productType=null;
        this.price=null;
    }
}
