/*
 * SpinalHDL
 * Copyright (c) Dolu, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */

package issue

import spinal.core._
import spinal.lib.slave

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

class Memory(memoryWidth : Bits, wordCount : Int, initFile : String) extends Component {
  val io = new Bundle {
    val sb = slave(SimpleBus(32,32))
    val sel = in Bool
  }

  val mem = new Mem(memoryWidth, wordCount)
  val rdy = Reg(Bool) init(False)
  val read = io.sb.SBvalid && io.sel && !io.sb.SBwrite
  val write = io.sb.SBvalid && io.sel && io.sb.SBwrite
  val intDBG = Bits(32 bits)

  if(initFile.isEmpty){
    println("Init RAM with 0")
    mem.init(List.fill(wordCount)(B(0,32 bits)))
  }
  else {
    println("Init RAM with initFile")
    mem.init(Tools.readmemh(initFile))
    //mem.init(List.fill(wordCount)(B(0,32 bits)))
    //mem.init(Tools.readBytesFromTxt(initFile))
  }

  intDBG := mem(B(0,9 bits).asUInt).resized
  io.sb.SBrdata := 0

  when(io.sel){
    mem.write(
      enable = write,
      address = io.sb.SBaddress(8 downto 0),
      data = io.sb.SBwdata
    )
    io.sb.SBrdata(5 downto 0) := mem.readSync(
      enable = read,
      address = io.sb.SBaddress(8 downto 0)
    )
    rdy := io.sb.SBvalid && io.sel
  }

  io.sb.SBready := rdy
}