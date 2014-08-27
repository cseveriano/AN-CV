#Clear all workspace
rm(list = ls(all = TRUE))
library(zoo)
library(forecast)
library(insol)

dirInputs = "../Data/INPUTS"
#dirClearSky = "../Data/ClearSky"
dirClearSky = "C:/Users/Carlos/Documents/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/Data/ClearSky"


#receiving commands
args <- commandArgs(trailingOnly = TRUE)
gsiIndex <- args[1]
gsi <- numeric(args[2])

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
gsiIndex = "2014-01-01 12:00"
gsi = 11.8
csIndex = substr(5, nchar(gsiIndex)) # retrieve "mm-dd hh:mi" part

ind = pmatch(csIndex,csdata$Tm)
csky = csdata$CS_GHI[ind]

Kt = gsi / csky

ind = if(ind == length(csdata$CS_GHI[ind])) 1 else i + 1
Persistence = Kt * csdata$CS_GHI[ind];

# 3 - Save File










