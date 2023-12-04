`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company:
// Engineer:
//
// Create Date: 12/01/2023 04:05:10 PM
// Design Name:
// Module Name: fpga
// Project Name:
// Target Devices:
// Tool Versions:
// Description:
//
// Dependencies:
//
// Revision:
// Revision 0.01 - File Created
// Additional Comments:
//
//////////////////////////////////////////////////////////////////////////////////


module fpga(
    input clk_in1_0,
    input RST_0,
    output [6:0] SEG_0,
	output [7:0] AN_0
    );
    wire CLKFBOUT, iPCK;

    wire [31:0] check;
    wire CLK_top, CLK_25MHz;

    BUFG iBUFG(.I(CLK_top), .O(CLK_25MHz));
    MMCME2_BASE #(
        .CLKFBOUT_MULT_F(50.0),
        .CLKFBOUT_PHASE(0.0),
        .CLKIN1_PERIOD(10.0), // 100Hz -> 10ns
        .CLKOUT0_DIVIDE_F(40.0),
        .CLKOUT1_DIVIDE(1),
        .CLKOUT2_DIVIDE(1),
        .CLKOUT3_DIVIDE(1),
        .CLKOUT4_DIVIDE(1),
        .CLKOUT5_DIVIDE(1),
        .CLKOUT6_DIVIDE(1),
        .CLKOUT0_DUTY_CYCLE(0.5),
        .CLKOUT1_DUTY_CYCLE(0.5),
        .CLKOUT2_DUTY_CYCLE(0.5),
        .CLKOUT3_DUTY_CYCLE(0.5),
        .CLKOUT4_DUTY_CYCLE(0.5),
        .CLKOUT5_DUTY_CYCLE(0.5),
        .CLKOUT6_DUTY_CYCLE(0.5),
        .CLKOUT0_PHASE(0.0),
        .CLKOUT1_PHASE(0.0),
        .CLKOUT2_PHASE(0.0),
        .CLKOUT3_PHASE(0.0),
        .CLKOUT4_PHASE(0.0),
        .CLKOUT5_PHASE(0.0),
        .CLKOUT6_PHASE(0.0),
        .CLKOUT4_CASCADE("FALSE"),
        .DIVCLK_DIVIDE(5),
        .REF_JITTER1(0.0),
        .STARTUP_WAIT("FALSE")
    ) MMCME2_BASE_inst (
        .CLKOUT0(CLK_top),
        .CLKOUT0B(),   // 1-bit output: Inverted CLKOUT0
        .CLKOUT1(),     // 1-bit output: CLKOUT1
        .CLKOUT1B(),   // 1-bit output: Inverted CLKOUT1
        .CLKOUT2(),     // 1-bit output: CLKOUT2
        .CLKOUT2B(),   // 1-bit output: Inverted CLKOUT2
        .CLKOUT3(),     // 1-bit output: CLKOUT3
        .CLKOUT3B(),   // 1-bit output: Inverted CLKOUT3
        .CLKOUT4(),     // 1-bit output: CLKOUT4
        .CLKOUT5(),     // 1-bit output: CLKOUT5
        .CLKOUT6(),     // 1-bit output: CLKOUT6
        .CLKFBOUT(CLKFBOUT),
        .CLKFBOUTB(), // 1-bit output: Inverted CLKFBOUT
        .LOCKED(),
        .CLKIN1(clk_in1_0),
        .PWRDWN(1'b0),       // 1-bit input: Power-down
        .RST(1'b0),             // 1-bit input: Reset
        .CLKFBIN(CLKFBOUT)
    );

    top top(
        .CLK(CLK_25MHz),
        .RST(RST_0),
        .tohost(check)
    );
    SEG_PUT seg_put(
        .CLK(clk_in1_0),
        .reset(RST_0),
        .check(check),
        .SEG(SEG_0),
        .AN(AN_0)
    );
endmodule
