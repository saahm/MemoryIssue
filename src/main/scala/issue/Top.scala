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

class Top(initHexfile:String) extends Component {
  val io = new Bundle {
  }
  val valid = Reg(Bool) init(True)
  val rdy = Bool
  val addr = Reg(UInt(32 bits)) init(0)
  val wdata = Reg(Bits(32 bits)) init(0)
  val rdata = Bits(32 bits)
  val sel = Reg(Bool) init(True)
  val wr = Reg(Bool) init(False)

  val ram = new Memory(32,512,initHexfile)

  ram.io.sb.SBvalid <> valid
  ram.io.sb.SBready <> rdy
  ram.io.sb.SBaddress <> addr
  ram.io.sb.SBwdata <> wdata
  ram.io.sb.SBrdata <> rdata
  ram.io.sb.SBwrite <> wr
  ram.io.sel <> sel

  val counter = new Area{
    val c = Reg(UInt(32 bits)) init(0)
    when(c === c.maxValue){
      c := 0
    }otherwise{
      c := c + 1
    }
  }
  addr := counter.c
}

//Generate the Memory's Verilog
object TopVerilog {
  def main(args: Array[String]) {
    SpinalConfig(targetDirectory = "rtl").generateVerilog(new Top("dumps/addsave.hex")).printPruned()
  }
}