package ru.mipt.diht.samples.model;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * @author s.chebotarev
 * @since 05.11.2015
 */
@XmlRootElement
@Data
public class Car {
    private String id;
    private String model;
    private Date dateOfManufacture;
    private int priceRur;
}
