package issue

import spinal.core._
import spinal.sim._
import spinal.core.sim._

import scala.util.Random

object TopSim {
  def main(args: Array[String]) {
    SimConfig
      .withWave
      .compile(new Top("dumps/addsave.hex"))
      .doSim{dut =>
        //Fork a process to generate the reset and the clock on the dut
        dut.clockDomain.forkStimulus(period = 10)

        for(idx <- 0 to 20){

          //Wait a rising edge on the clock
          dut.clockDomain.waitRisingEdge()

        }
    }
  }
}
