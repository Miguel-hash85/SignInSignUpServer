/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;
import interfaces.Signable;

/**
 *  Class that generate implementation of the interface signable.
 * @author Zeeshan Yaqoob
 */
public class SignableFactory {

    /**
     * This method creates and return an object of DaoSignableImplementation.
     * @return an object of Signable that would be DaoSignableImplementation;
     */
    public Signable getSignableImplementation(){
        Signable signable= new DaoSignableImplementation();
        return signable;
    }
}
