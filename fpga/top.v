`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 11/28/2023 03:04:21 PM
// Design Name: 
// Module Name: top
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


module top(
    input CLK,
    input RST,
    output [31:0] tohost
    );
    wire io_icache_axi4lite_ar_ready;
    wire io_icache_axi4lite_aw_ready;
    wire io_icache_axi4lite_b_valid;
    wire [2:0] io_icache_axi4lite_b_bits_resp;
    wire io_icache_axi4lite_r_valid;
    wire [31:0] io_icache_axi4lite_r_bits_data;
    wire [2:0] io_icache_axi4lite_r_bits_resp;
    // wire io_icache_axi4lite_w_ready;
    wire io_dcache_axi4lite_ar_ready;
    wire io_dcache_axi4lite_aw_ready;
    wire io_dcache_axi4lite_b_valid;
    wire  [2:0]  io_dcache_axi4lite_b_bits_resp;
    wire         io_dcache_axi4lite_r_valid;
    wire  [63:0] io_dcache_axi4lite_r_bits_data;
    wire  [2:0]  io_dcache_axi4lite_r_bits_resp;
    wire         io_dcache_axi4lite_w_ready;
    // wire  [63:0] io_reset_vector;
    // wire io_hartid;
    wire        io_icache_axi4lite_ar_valid;
    wire [63:0] io_icache_axi4lite_ar_bits_addr;
    wire [2:0]  io_icache_axi4lite_ar_bits_prot;
    wire        io_icache_axi4lite_aw_valid;
    wire [63:0] io_icache_axi4lite_aw_bits_addr;
    wire [2:0]  io_icache_axi4lite_aw_bits_prot;
    wire        io_icache_axi4lite_b_ready;
    wire io_icache_axi4lite_r_ready;
    wire io_icache_axi4lite_w_valid;
    wire [31:0] io_icache_axi4lite_w_bits_data;
    wire [3:0]  io_icache_axi4lite_w_bits_strb;
    wire        io_dcache_axi4lite_ar_valid;
    wire [63:0] io_dcache_axi4lite_ar_bits_addr;
    wire [2:0]  io_dcache_axi4lite_ar_bits_prot;
    wire        io_dcache_axi4lite_aw_valid;
    wire [63:0] io_dcache_axi4lite_aw_bits_addr;
    wire [2:0]  io_dcache_axi4lite_aw_bits_prot;
    wire        io_dcache_axi4lite_b_ready;
    wire io_dcache_axi4lite_r_ready;
    wire io_dcache_axi4lite_w_valid;
    wire [63:0] io_dcache_axi4lite_w_bits_data;
    wire [7:0]  io_dcache_axi4lite_w_bits_strb;
    // wire tohost_valid;
    wire [31:0] tohost_bits;
    
    Core core(
      .clock(CLK),
      .reset(!RST),
      .io_icache_axi4lite_ar_ready(io_icache_axi4lite_ar_ready),
      .io_icache_axi4lite_aw_ready(io_icache_axi4lite_aw_ready),
      .io_icache_axi4lite_b_valid(io_icache_axi4lite_b_valid),
      .io_icache_axi4lite_b_bits_resp(io_icache_axi4lite_b_bits_resp),
      .io_icache_axi4lite_r_valid(io_icache_axi4lite_r_valid),
      .io_icache_axi4lite_r_bits_data(io_icache_axi4lite_r_bits_data),
      .io_icache_axi4lite_r_bits_resp(io_icache_axi4lite_r_bits_resp),
      .io_icache_axi4lite_w_ready(),
      .io_dcache_axi4lite_ar_ready(io_dcache_axi4lite_ar_ready),
      .io_dcache_axi4lite_aw_ready(io_dcache_axi4lite_aw_ready),
      .io_dcache_axi4lite_b_valid(io_dcache_axi4lite_b_valid),
      .io_dcache_axi4lite_b_bits_resp(io_dcache_axi4lite_b_bits_resp),
      .io_dcache_axi4lite_r_valid(io_dcache_axi4lite_r_valid),
      .io_dcache_axi4lite_r_bits_data(io_dcache_axi4lite_r_bits_data),
      .io_dcache_axi4lite_r_bits_resp(io_dcache_axi4lite_r_bits_resp),
      .io_dcache_axi4lite_w_ready(io_dcache_axi4lite_w_ready),
      .io_reset_vector(64'h0),
      .io_hartid(64'h0),
      .io_icache_axi4lite_ar_valid(io_icache_axi4lite_ar_valid),
      .io_icache_axi4lite_ar_bits_addr(io_icache_axi4lite_ar_bits_addr),
      .io_icache_axi4lite_ar_bits_prot(io_icache_axi4lite_ar_bits_prot),
      .io_icache_axi4lite_aw_valid(io_icache_axi4lite_aw_valid),
      .io_icache_axi4lite_aw_bits_addr(io_icache_axi4lite_aw_bits_addr),
      .io_icache_axi4lite_aw_bits_prot(io_icache_axi4lite_aw_bits_prot),
      .io_icache_axi4lite_b_ready(io_icache_axi4lite_b_ready),
      .io_icache_axi4lite_r_ready(io_icache_axi4lite_r_ready),
      .io_icache_axi4lite_w_valid(io_icache_axi4lite_w_valid),
      .io_icache_axi4lite_w_bits_data(io_icache_axi4lite_w_bits_data),
      .io_icache_axi4lite_w_bits_strb(io_icache_axi4lite_w_bits_strb),
      .io_dcache_axi4lite_ar_valid(io_dcache_axi4lite_ar_valid),
      .io_dcache_axi4lite_ar_bits_addr(io_dcache_axi4lite_ar_bits_addr),
      .io_dcache_axi4lite_ar_bits_prot(io_dcache_axi4lite_ar_bits_prot),
      .io_dcache_axi4lite_aw_valid(io_dcache_axi4lite_aw_valid),
      .io_dcache_axi4lite_aw_bits_addr(io_dcache_axi4lite_aw_bits_addr),
      .io_dcache_axi4lite_aw_bits_prot(io_dcache_axi4lite_aw_bits_prot),
      .io_dcache_axi4lite_b_ready(io_dcache_axi4lite_b_ready),
      .io_dcache_axi4lite_r_ready(io_dcache_axi4lite_r_ready),
      .io_dcache_axi4lite_w_valid(io_dcache_axi4lite_w_valid),
      .io_dcache_axi4lite_w_bits_data(io_dcache_axi4lite_w_bits_data),
      .io_dcache_axi4lite_w_bits_strb(io_dcache_axi4lite_w_bits_strb)
    );
    Icache_for_Verilator icache(
      .clock(CLK),
      .reset(!RST),
      .io_ar_valid(io_icache_axi4lite_ar_valid),
      .io_ar_bits_addr(io_icache_axi4lite_ar_bits_addr),
      .io_ar_bits_prot(io_icache_axi4lite_ar_bits_prot),
      .io_aw_valid(io_icache_axi4lite_aw_valid),
      .io_aw_bits_addr(io_icache_axi4lite_aw_bits_addr),
      .io_aw_bits_prot(io_icache_axi4lite_aw_bits_prot),
      .io_b_ready(io_icache_axi4lite_b_ready),
      .io_r_ready(io_icache_axi4lite_r_ready),
      .io_w_valid(io_icache_axi4lite_w_valid),
      .io_w_bits_data(io_icache_axi4lite_w_bits_data),
      .io_w_bits_strb(io_icache_axi4lite_w_bits_strb),
      .io_ar_ready(io_icache_axi4lite_ar_ready),
      .io_aw_ready(io_icache_axi4lite_aw_ready),
      .io_b_valid(io_icache_axi4lite_b_valid),
      .io_b_bits_resp(io_icache_axi4lite_b_bits_resp),
      .io_r_valid(io_icache_axi4lite_r_valid),
      .io_r_bits_data(io_icache_axi4lite_r_bits_data),
      .io_r_bits_resp(io_icache_axi4lite_r_bits_resp),
      .io_w_ready()
    );
    Dcache_for_Verilator dcache(
      .clock(CLK),
      .reset(!RST),
      .io_ar_valid(io_dcache_axi4lite_ar_valid),
      .io_ar_bits_addr(io_dcache_axi4lite_ar_bits_addr),
      .io_ar_bits_prot(io_dcache_axi4lite_ar_bits_prot),
      .io_aw_valid(io_dcache_axi4lite_aw_valid),
      .io_aw_bits_addr(io_dcache_axi4lite_aw_bits_addr),
      .io_aw_bits_prot(io_dcache_axi4lite_aw_bits_prot),
      .io_b_ready(io_dcache_axi4lite_b_ready),
      .io_r_ready(io_dcache_axi4lite_r_ready),
      .io_w_valid(io_dcache_axi4lite_w_valid),
      .io_w_bits_data(io_dcache_axi4lite_w_bits_data),
      .io_w_bits_strb(io_dcache_axi4lite_w_bits_strb),
      .io_ar_ready(io_dcache_axi4lite_ar_ready),
      .io_aw_ready(io_dcache_axi4lite_aw_ready),
      .io_b_valid(io_dcache_axi4lite_b_valid),
      .io_b_bits_resp(io_dcache_axi4lite_b_bits_resp),
      .io_r_valid(io_dcache_axi4lite_r_valid),
      .io_r_bits_data(io_dcache_axi4lite_r_bits_data),
      .io_r_bits_resp(io_dcache_axi4lite_r_bits_resp),
      .io_w_ready(io_dcache_axi4lite_w_ready),
      .debug_valid(),
      .debug_bits(tohost_bits)
    );

    assign tohost = tohost_bits;
endmodule
