/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author Zeeshan Yaqoob
 */
import interfaces.Signable;

/**
 *
 * @author Zeeshan Yaqoob
 */
public class SignableFactory {

    /**
     *
     * @return an object of Signable that would be DaobleImplementation;
     */
    public Signable getSignableImplementation(){
        Signable signable= new DaoSignableImplementation();
        return signable;
    }
}
