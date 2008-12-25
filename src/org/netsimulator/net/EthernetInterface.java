/*
NET-Simulator -- Network simulator.
Copyright (C) 2006 Maxim Tereshin <maxim-tereshin@yandex.ru>

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.
            
This program is distributed in the hope that it will be useful, but 
WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.
            
You should have received a copy of the GNU General Public License along 
with this program; if not, write to the Free Software Foundation, 
Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/

package org.netsimulator.net;


import java.util.*;
import java.util.logging.*;
import org.netsimulator.util.IdGenerator;

public class EthernetInterface 
        implements IP4EnabledInterface, NetworkDevice
{
    private static Logger logger = 
            Logger.getLogger("org.netsimulator.net.EthernetInterface");
    private int id;
    private IdGenerator idGenerator;   
    private Router router;
    private Media media;
    private MACAddress macAddress;
    private IP4Address inetAddress;
    private IP4Address broadcastAddress;
    private IP4Address netmaskAddress;
    private int status;
    private int bandwidth;
    private String encap;
    private int rxBytes;
    private int txBytes;
    private int rxPackets;
    private int txPackets;
    private int rxPacketsErrors;
    private int txPacketsErrors;
    private int rxDroped;
    private int txDroped;
    private ARPCache arpCache;
    private String name;
    private ArrayList<TransferPacketListener> transferPacketListeners;

    
    public EthernetInterface(
            IdGenerator idGenerator, 
            MACAddress address, 
            String name)
    {
        this(
                idGenerator,
                idGenerator.getNextId(),
                address,
                name
            );
    }


    public EthernetInterface(
            IdGenerator idGenerator, 
            int id, 
            MACAddress address, 
            String name)
    {
        this.idGenerator = idGenerator;
        this.id = id;
        this.macAddress = address;
        this.name = name;
        encap = "Ethernet  HWaddr "+address;
        status = Interface.DOWN;
        
        arpCache = new ARPCache(10);
        transferPacketListeners = new ArrayList<TransferPacketListener>();
    }
    
    
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    
    public String getName()
    {
        return name;
    }
    
    
    @Override
    public String toString()
    {
        return getName();
    }
        
    
    
    public void setRouter(Router router)
    {
        this.router = router;
    }



    public void connectMedia(Media media)
    {
        this.media = media;
    }


    public void disconnectMedia()
    {
        this.media = null;
    }


    
    
    
    public void recivePacket(Layer2Packet l2packet)
    {
        if(getStatus() == Interface.DOWN){ return; }
        
        if(router == null /*||
           !(packet instanceof Layer2Packet)*/)
        {
            rxDroped++;
            return;
        }

        //Layer2Packet l2packet = (Layer2Packet)packet;

        if(this.macAddress.equals((MACAddress)l2packet.getDestinationAddress()) ||
           ((MACAddress)l2packet.getDestinationAddress()).isBroadcast())
        {
            
            switch(l2packet.getEtherType())
            {
                case Protocols.ARP :
                    ARPPacket arpPacket= (ARPPacket)l2packet.getData();
                    switch(arpPacket.getOperation())        
                    {
                        case ARPPacket.REQUEST :
                            try
                            {        
                                processArpRequest(arpPacket);
                            }catch(AddressException ae)
                            {
                                ae.printStackTrace();
                            }
                            rxPackets++;
                            break;
                        case ARPPacket.REPLAY :
                            try
                            {
                                processArpReplay(arpPacket);
                            }catch(AddressException ae)
                            {
                                ae.printStackTrace();
                            }
                            rxPackets++;
                            break;
                        default :
                            rxPacketsErrors++;
                    }
                    break;
                    
                case Protocols.IP :
                    router.routePacket((Packet)l2packet.getData());
                    rxPackets++;
                    break;

                default :
                    rxPacketsErrors++;
            }
        }
        
        for(Iterator<TransferPacketListener> i = transferPacketListeners.iterator(); i.hasNext(); )
        {
            TransferPacketListener listener = i.next();
            listener.PacketTransfered(l2packet);
            listener.PacketReceived(l2packet);
        }        
    }


    public void transmitPacket(Layer2Packet packet)
    {
        if(getStatus() == Interface.DOWN){ return; }
        
        if(media != null)
        {
            media.transmitPacket(this, packet);
            txPackets++;
            for(Iterator<TransferPacketListener> i = transferPacketListeners.iterator(); i.hasNext(); )
            {
                TransferPacketListener listener = i.next();
                listener.PacketTransfered(packet);
                listener.PacketTransmitted(packet);
            }            
        }else
        {
            txPacketsErrors++;
        }
    }


    public void setStatus(int status) throws ChangeInterfacePropertyException
    {
        
        /*
         *  Should we drop counters if the interface is coming down?
         */
  
        if( router instanceof IP4Router )
        {
            RoutingTable rt = ((IP4Router)router).getRoutingTable();
            switch( status )
            {
                case UP:
                    logger.fine("Setting "+getName()+" UP, address "+getInetAddress()+
                            ", mask "+getNetmaskAddress()+", broadcast "+getBroadcastAddress() );
                    if( this.status == UP ) { return; }
                    if( getInetAddress() == null ) 
                    {
                        throw new ChangeInterfacePropertyException( "Can not change inteface status, set inet address befor." );
                    }
                    if( getNetmaskAddress() == null ) 
                    {
                        setNetmaskAddress( evaluateNetmaskAddress( getInetAddress() ) );
                    }
                    if( getInetAddress() == getNetworkAddress() ) 
                    {
                        throw new ChangeInterfacePropertyException( "Inet address can not be equal network one." );
                    }
                    if( getBroadcastAddress() == null ) 
                    {
                        setBroadcastAddress( evaluateBroadcastAddress( getInetAddress(), getNetmaskAddress() ) );
                    }
                    if( getInetAddress() == getBroadcastAddress() ) 
                    {
                        throw new ChangeInterfacePropertyException( "Inet address can not be equal broadcast one." );
                    }
                    try
                    {
                        rt.addRoute( getNetworkAddress(), 
                                     getNetmaskAddress(),
                                     null,
                                     1, 
                                     this );
                    } catch ( NotAllowedAddressException e ) 
                    {
                        throw new ChangeInterfacePropertyException( e.getMessage() );
                    }
                    break;
                case DOWN:
                    logger.fine("Setting "+getName()+" DOWN, address "+getInetAddress()+
                            ", mask "+getNetmaskAddress()+", broadcast "+getBroadcastAddress() );
                    if( this.status == DOWN ) { return; }
                    rt.deleteRoute( getNetworkAddress(), 
                                 getNetmaskAddress(),
                                 null,
                                 1, 
                                 this );
                    break;
                default:
                    throw new IllegalStateException("Unknown status.");
            }
        }

        this.status = status;
    }


    public int getStatus()
    {
        return status;
    }


    public void setBandwidth(int bandwidth)
    {
        this.bandwidth = bandwidth;
    }


    public int getBandwidth()
    {
        return bandwidth;
    }

    public IP4Address getInetAddress()
    {
        return inetAddress;
    }

    public IP4Address getBroadcastAddress()
    {
        return broadcastAddress;
    }

    public IP4Address getNetmaskAddress()
    {
        return netmaskAddress;
    }

    
    public void setBroadcastAddress(IP4Address address) 
            throws ChangeInterfacePropertyException
    {
        if( getStatus() == UP )
        {
            setStatus( DOWN );
            this.broadcastAddress = address;
            setStatus( UP );
        } else
        {
            this.broadcastAddress = address;
        }
    }


    public void setNetmaskAddress(IP4Address address) 
            throws ChangeInterfacePropertyException
    {
        if( getStatus() == UP )
        {
            setStatus( DOWN );
            this.netmaskAddress = address;
            setStatus( UP );
        } else
        {
            this.netmaskAddress = address;
        }
    }

    
    
    public void setInetAddress(IP4Address address) 
            throws ChangeInterfacePropertyException 
    {
        if( getStatus() == UP )
        {
            setStatus( DOWN );
            this.inetAddress = address;
            setStatus( UP );
        } else
        {
            this.inetAddress = address;
        }
    }
    

    public String getEncap()
    {
        return encap;
    }

    public int getRXBytes()
    {
        return rxBytes;
    }

    public int getRXDroped()
    {
        return rxDroped;
    }

    public int getRXPackets()
    {
        return rxPackets;
    }

    public int getRXPacketsErrors()
    {
        return rxPacketsErrors;
    }
    
    
    public int getTXBytes()
    {
        return txBytes;
    }

    public int getTXDroped()
    {
        return txDroped;
    }

    public int getTXPackets()
    {
        return txPackets;
    }

    public int getTXPacketsErrors()
    {
        return txPacketsErrors;
    }
    
    
    public MACAddress getMACAddress()
    {
        return macAddress;
    }

    
    
    public MACAddress resolveAddress(IP4Address ip4address)
    {
        MACAddress macaddress = arpCache.get(ip4address);
        
        if( macaddress == null )
        {
            try
            {
                makeArpRequest(ip4address);
                Thread.sleep(100);
            }catch(AddressException ae)
            {
                ae.printStackTrace();
            }catch(InterruptedException ie)
            {
                ie.printStackTrace();
            }
            macaddress = arpCache.get(ip4address);
        }

        return macaddress;
    }
    
    
    
    
    public void makeArpRequest(IP4Address address)
    throws AddressException
    {
        Layer2Packet arpRequestPacket = 
            new Layer2Packet( macAddress, 
                              new MACAddress("FF:FF:FF:FF:FF:FF"),
                              Protocols.ARP, 
                              new ARPPacket(macAddress, address, ARPPacket.REQUEST) );
        
        transmitPacket(arpRequestPacket);
    }



    
    public void makeArpReplay(ARPPacket arpPacket)
    throws AddressException
    {
        arpPacket.setOperation(ARPPacket.REPLAY);
        arpPacket.setResolvedAddress(getMACAddress());
        
        Layer2Packet arpReplayPacket = 
            new Layer2Packet( macAddress, 
                              arpPacket.getSourceMacAddress(),
                              Protocols.ARP, 
                              arpPacket );
        
        transmitPacket(arpReplayPacket);
    }


    
    protected void processArpRequest(ARPPacket arpPacket)
    throws AddressException
    {
        if( inetAddress!=null && 
            arpPacket.getAddressToResolve().equals(inetAddress))
        {
            makeArpReplay(arpPacket);
        }
    }
    
    
    
    protected void processArpReplay(ARPPacket arpPacket)
    throws AddressException
    {
        arpCache.put (arpPacket.getAddressToResolve(), arpPacket.getResolvedAddress());
    }

    
    
    public ARPCache getArpCache()
    {
        return arpCache;
    }
    
    
    
    
    public IP4Address getNetworkAddress()
    {
        if(inetAddress==null || netmaskAddress==null)
        {
            return null;
        }else
        {
            return new IP4Address(inetAddress.toIntValue() & netmaskAddress.toIntValue());
        }
    }
    
    
    
    
    
    public void transmitPacket(IP4Packet packet, IP4Address destination)
    {
        MACAddress macAddress = resolveAddress(destination);
        
        if(macAddress == null)
        {
            logger.info(getId()+" Can not resolv MACAddress for: "+destination);
            return;
        }
        
        Layer2Packet l2packet = null;
        try
        {
            l2packet = 
            new Layer2Packet( getMACAddress(), 
                              macAddress,
                              Protocols.IP, 
                              packet );
        }catch(AddressException ae)
        {
            ae.printStackTrace();
            return;
        }
        
        transmitPacket(l2packet);
    }
   
    
    
    public int getId()
    {
        return id;
    }
    
    public IdGenerator getIdGenerator()
    {
        return idGenerator;
    }

    public void removeTransferPacketListener(TransferPacketListener listener)
    {
        transferPacketListeners.remove(listener);
    }

    public void addTransferPacketListener(TransferPacketListener listener)
    {
        transferPacketListeners.add(listener);
    }


    /**
     * Trying to evalute netmask for given address.
     */
    private IP4Address evaluateNetmaskAddress( IP4Address address ) 
            throws ChangeInterfacePropertyException
    {
        IP4Address netmask = null;
        
        try 
        {
            if( address.isClassA() )
            {
                netmask = new IP4Address("255.0.0.0");
                logger.fine(hashCode()+": the address "+address+" is of class A, so netmask is "+netmask);
            }

            if( address.isClassB() )
            {
                netmask = new IP4Address("255.255.0.0");
                logger.fine(hashCode()+": the address "+address+" is of class B, so netmask is "+netmask);
            }

            if( address.isClassC() )
            {
                netmask = new IP4Address("255.255.255.0");
                logger.fine(hashCode() + ": the address " + address + " is of class C, so netmask is " + netmask);
            }
        } catch (AddressException ex) 
        {
            throw new IllegalStateException( ex );
        }

        if( netmask==null )
        {
            throw new ChangeInterfacePropertyException( "Can not evaluate netmask address." );
        }

        return netmask;
    }

    
    /**
     * Trying to evalute broadcast for given address and netmask.
     */
    private IP4Address evaluateBroadcastAddress( IP4Address address, IP4Address netmask ) 
    {
        int broadcastInt = address.toIntValue() | ~netmask.toIntValue();
        return new IP4Address( broadcastInt );
    }
}
