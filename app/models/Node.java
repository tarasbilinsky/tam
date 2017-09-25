package models;

import base.models.ModelBase;
import io.ebean.Ebean;
import io.ebean.annotation.Transactional;
import play.Logger;

import javax.persistence.Entity;


@Entity
public class Node extends ModelBase {
    public String ipv4;

    public int deployGroup = -1;

    @Transactional
    public static Node findByIp(String ipv4, int deployGroup){

        Node n = Ebean.createQuery(Node.class).select("*").where().eq("ipv4",ipv4).findOne();
        if(n==null){
            n = new Node();
            n.ipv4 = ipv4;
            n.deployGroup = deployGroup;
            Logger.warn("Node: "+ipv4+" group "+deployGroup);
            n.save();
            Logger.warn("Node: id"+n.id);
        } else {
            if(n.deployGroup!=deployGroup){
                n.deployGroup = deployGroup;
                n.save();
            }
            Logger.warn("Node: existing "+n.ipv4+" id:"+n.id+" group "+deployGroup);
        }
        return n;
    }

    @Transactional
    public static void deleteById(Long id){
        Node n = Ebean.createQuery(Node.class).select("*").where().eq("id",id).findOne();
        if(n==null) return;
        Logger.warn("Node delete "+n.ipv4+" id "+n.id+" group "+n.deployGroup);
        n.delete();
    }

    @Override
    public void save(){
        if(ipv4!=null) super.save();
    }
}
