use std::time::{Instant};
use std::env;


fn on_mult(m_ar:usize, m_br:usize){
    let pha: Vec<f64> = vec![1.0; m_ar * m_ar];
    let mut phb: Vec<f64> = vec![0.0; m_br * m_br];
    let mut phc: Vec<f64> = vec![0.0; m_ar * m_ar];

    for i in 0..m_br {
        for j in 0..m_br {
            phb[i * m_br + j] = (i + 1) as f64;
        }
    }

    let time1 = Instant::now();

    for i in 0..m_ar{
        for j in 0..m_ar{
            for k in 0..m_br{
              phc[i * m_ar + j] += pha[i * m_ar + k] * phb[k * m_br +j];
            }
        }
    }


    let time2 = Instant::now();

    let duration = time2.duration_since(time1);
    let elapsed_secs = duration.as_secs_f64();
    let st = format!("Time:{:.3}\n", elapsed_secs);
    
    print!("{}", st);
    /*
    // display 10 elements of the result matrix to verify correctness
    println!("Result matrix: ");
    for i in 0..1 {
        for j in 0..std::cmp::min(10, m_br) {
            print!("{} ", phc[i * m_ar + j]);
        }
    }
    println!();
    */

}

fn on_mult_line(m_ar:usize, m_br:usize){
    let pha: Vec<f64> = vec![1.0; m_ar * m_ar];
    let mut phb: Vec<f64> = vec![0.0; m_br * m_br];
    let mut phc: Vec<f64> = vec![0.0; m_ar * m_ar];

    for i in 0..m_br {
        for j in 0..m_br {
            phb[i * m_br + j] = (i + 1) as f64;
        }
    }

    let time1 = Instant::now();

    for i in 0..m_ar{
        for k in 0..m_br{
            for j in 0..m_ar{
                phc[i * m_ar + j] += pha[i * m_ar + k] * phb[k * m_br +j];
            }
        }
    }


    let time2 = Instant::now();

    let duration = time2.duration_since(time1);
    let elapsed_secs = duration.as_secs_f64();
    let st = format!("Time:{:.3}\n", elapsed_secs);
    print!("{}", st);

    // display 10 elements of the result matrix to verify correctness
    /*
    println!("Result matrix: ");
    for i in 0..1 {
        for j in 0..std::cmp::min(10, m_br) {
            print!("{} ", phc[i * m_ar + j]);
        }
    }
    println!();
    */
}



fn main() {
    let args: Vec<String> = env::args().collect();
    if args.len() != 3 {
        println!("Usage: {} <arg1> <arg2>", args[0]);
        std::process::exit(1);
    }

    let op: usize  = args[1].parse::<usize>().unwrap();
    let lin: usize = args[2].parse::<usize>().unwrap();
    let col: usize = lin;

    //1. Multiplication
    //2. Line Multiplication

    match op {
        1 => on_mult(lin, col),
        2 => on_mult_line(lin, col),
        _ => println!("Invalid operation"),
    }
}
