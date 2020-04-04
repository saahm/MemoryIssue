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
import spinal.lib.fsm.{EntryPoint, State, StateMachine}
import spinal.lib.master

class RTLCore() extends Component {
  //IO Definition
  val io = new Bundle {
    // simple bus access
    val sb = master(SimpleBus(32,32))
    val done = out Bool
  }

  val counterLogic = new Area{
    // data signals
    val counter = Reg(UInt(32 bits)) init(0)
    // control signals
    val cEna = Bool
    when(cEna){
      counter := counter + 4 //two 16 bit numbers get loaded, move pointer by 4
    }
  }

  val datapath = new Area{
    import FSMStates._
    // data signals
    val s1 = Reg(UInt(16 bits)) init(0)
    val s2 = Reg(UInt(16 bits)) init(0)
    val res1 = Reg(UInt(32 bits)) init(0)
    val res2 = Reg(UInt(32 bits)) init(0)
    val s1_gt_s2 = Bool
    val s1_lt_s2 = Bool
    val s1_eq_s2 = Bool
    val add_res = UInt(32 bits)
    val add_a = UInt(16 bits)
    val add_b = UInt(16 bits)

    // control signals
    val done = Bool
    val fsm = FSMStates()
    val data = UInt(32 bits)

    data := io.sb.SBwdata.asUInt
    done := False
    switch(fsm){
      is(INIT){
        s1 := 0
        s2 := 0
        res1 := 0
        res2 := 0
      }
      is(FETCH){
        s1 := data(31 downto 16)
        s2 := data(15 downto 0)
      }
      is(CALC){
        when(s1_gt_s2){
          res2 := add_res
        }elsewhen(s1_eq_s2){
          //do nothing
        }otherwise{
          res1 := add_res
        }
      }
      is(DONE){
        done := True
      }
    }
    add_a := (s1_gt_s2 & !s1_lt_s2) ? s2 | s1
    add_b := (!s1_gt_s2 & s1_lt_s2) ? res2(15 downto 0) | s2
    add_res := (add_a +^ add_b).resized
    s1_gt_s2 := s1 > s2
    s1_lt_s2 := s1 < s2
    s1_eq_s2 := (s1_gt_s2 === False & s1_lt_s2 === False)
  }

  val controlFSM = new Area{
    import FSMStates._

    val memAddr = Reg(UInt(32 bits)) init(0)
    val memValid = Reg(Bool) init(False)
    val memReady = Bool
    val memWrite = Reg(Bool) init(False)
    val memWData = Reg(Bits(32 bits)) init(0)

    val state = RegInit(INIT)

    memReady := io.sb.SBready
    counterLogic.cEna := False
    memAddr := counterLogic.counter
    switch(state){
      is(INIT){
        state := FETCH
      }
      is(FETCH){
        memValid := True
        when(memReady){
          memValid := False
          counterLogic.cEna := True
          state := CALC
        }
      }
      is(CALC){
        counterLogic.cEna := False
        when(datapath.s1_eq_s2 === True){
          state := DONE
        }otherwise{
          state := FETCH
        }
      }
      is(DONE){
        state := INIT
      }
    }
  }
  io.sb.SBaddress := controlFSM.memAddr
  io.sb.SBwdata := controlFSM.memWData
  io.sb.SBvalid := controlFSM.memValid
  io.sb.SBwrite := controlFSM.memWrite
  io.done := datapath.done
}

object FSMStates extends SpinalEnum {
  val INIT, FETCH, CALC, DONE = newElement()
}
