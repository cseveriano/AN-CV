#Clear all workspace
rm(list = ls(all = TRUE))
library(zoo)
library(forecast)
library(insol)

dirInputs = "../Data/INPUTS"
#dirClearSky = "../Data/ClearSky"
dirClearSky = "C:/Users/Carlos/Documents/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/Data/ClearSky"


#receiving commands
#args <- commandArgs(trailingOnly = TRUE)
#rnorm(n=as.numeric(args[1]), mean=as.numeric(args[2]))

## 1 - Load ClearSky
csfiles = dir(dirClearSky)

csdata = NULL

for(x in csfiles)
{
    filepath = paste(dirClearSky, x, sep = "/")
    temp = read.table(filepath, header = TRUE, sep = "\t")
    csdata = rbind(csdata,temp)
}


## 2 - Calculate Persistence
csIndex = "2013-01-01 12:00"
csValue = csdata$CS_GHI[pmatch(csIndex,csdata$Tm)]

## 3 - Save File










